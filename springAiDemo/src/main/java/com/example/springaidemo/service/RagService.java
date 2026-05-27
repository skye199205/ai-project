package com.example.springaidemo.service;

import com.example.springaidemo.request.RagAskRequest;
import com.example.springaidemo.request.RagIngestArticleRequest;
import com.example.springaidemo.request.RagIngestRequest;
import com.example.springaidemo.response.RagAskResponse;
import com.example.springaidemo.response.RagIngestArticleResponse;
import com.example.springaidemo.response.RagIngestDocumentResponse;
import com.example.springaidemo.response.RagIngestResponse;
import com.example.springaidemo.response.RagSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RAG：文本/长文入库、检索增强问答等业务逻辑。
 */
@Service
public class RagService {

    /**
     * 与 {@link com.example.springaidemo.controller.ChatController} 相同方式构建，仅在本服务内配合
     * QA Advisor 做检索增强
     */
    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;
    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter;
    private final DocumentParseService documentParseService;

    public RagService(
            ChatClient.Builder chatClientBuilder,
            QuestionAnswerAdvisor questionAnswerAdvisor,
            VectorStore vectorStore,
            TokenTextSplitter tokenTextSplitter,
            DocumentParseService documentParseService) {
        this.chatClient = Objects.requireNonNull(chatClientBuilder, "ChatClient.Builder 未注入").build();
        this.questionAnswerAdvisor = Objects.requireNonNull(questionAnswerAdvisor, "QuestionAnswerAdvisor 未注入");
        this.vectorStore = Objects.requireNonNull(vectorStore, "VectorStore 未注入");
        this.tokenTextSplitter = Objects.requireNonNull(tokenTextSplitter, "TokenTextSplitter 未注入");
        this.documentParseService = Objects.requireNonNull(documentParseService, "DocumentParseService 未注入");
    }

    /**
     * 将多段文本写入向量库。
     */
    public RagIngestResponse ingest(RagIngestRequest request) {
        List<Document> documents = request.texts().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Document::new)
                .toList();
        if (documents.isEmpty()) {
            return new RagIngestResponse(0);
        }
        vectorStore.add(documents);
        return new RagIngestResponse(documents.size());
    }

    /**
     * 上传 PDF / Word / TXT / Markdown，解析正文后分块写入向量库。
     */
    public RagIngestDocumentResponse ingestDocument(MultipartFile file, String title) {
        DocumentParseService.ParsedDocument parsed = documentParseService.parse(file);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ingestType", "document");
        metadata.put("fileName", parsed.fileName());
        metadata.put("fileType", parsed.fileType());
        if (StringUtils.hasText(title)) {
            metadata.put("title", title.trim());
        }

        RagIngestArticleResponse result = ingestLongText(parsed.content(), metadata);
        return new RagIngestDocumentResponse(
                parsed.fileName(),
                parsed.fileType(),
                result.chunkCount(),
                result.contentLength());
    }

    /**
     * 传入整篇长文，服务端按 token 自动分块后写入向量库。
     */
    public RagIngestArticleResponse ingestArticle(RagIngestArticleRequest request) {
        String content = Objects.requireNonNull(request.content(), "content").trim();
        if (content.isEmpty()) {
            return new RagIngestArticleResponse(0, 0);
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ingestType", "article");
        if (request.title() != null && !request.title().isBlank()) {
            metadata.put("title", request.title().trim());
        }

        return ingestLongText(content, metadata);
    }

    private RagIngestArticleResponse ingestLongText(String content, Map<String, Object> metadata) {
        Document article = new Document(content, metadata);
        List<Document> chunks = tokenTextSplitter.split(article);
        if (chunks.isEmpty()) {
            return new RagIngestArticleResponse(0, content.length());
        }

        vectorStore.add(chunks);
        return new RagIngestArticleResponse(chunks.size(), content.length());
    }

    /**
     * RAG 问答：检索相关片段，由大模型结合上下文生成回答，并返回引用摘要。
     */
    public RagAskResponse ask(RagAskRequest request) {
        String question = Objects.requireNonNull(request.question(), "question").trim();
        ChatClientResponse clientResponse = chatClient.prompt()
                .system("你是问答助手。请严格根据随后提供的「上下文片段」作答；若上下文不足以回答，请明确说明并不要编造事实。"
                        + "回答使用中文，简洁有条理。")
                .advisors(questionAnswerAdvisor)
                .user(Objects.requireNonNull(question, "question"))
                .call()
                .chatClientResponse();

        ChatResponse chatResponse = Objects.requireNonNull(clientResponse.chatResponse(), "chatResponse 为空");
        String answer = Objects.requireNonNull(
                Objects.requireNonNull(chatResponse.getResult(), "生成结果为空").getOutput().getText(),
                "模型返回为空");
        List<RagSource> sources = extractSources(clientResponse);
        return new RagAskResponse(answer, sources);
    }

    private static List<RagSource> extractSources(ChatClientResponse clientResponse) {
        Object raw = clientResponse.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<RagSource> out = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Document doc)) {
                continue;
            }
            String text = doc.getText();
            if (text == null) {
                text = "";
            }
            String excerpt = text.length() > 500 ? text.substring(0, 500) + "…" : text;
            out.add(new RagSource(excerpt, doc.getScore()));
        }
        return List.copyOf(out);
    }
}
