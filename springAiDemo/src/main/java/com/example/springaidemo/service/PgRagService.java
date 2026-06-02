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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 PostgreSQL pgvector 的 RAG 业务（与 {@link RagService} Chroma 路隔离）。
 */
@Service
@ConditionalOnBean(name = "pgVectorStore")
public class PgRagService {

    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor pgQuestionAnswerAdvisor;
    private final VectorStore pgVectorStore;
    private final TokenTextSplitter tokenTextSplitter;
    private final DocumentParseService documentParseService;
    private final PgDocumentIndexSyncService pgDocumentIndexSyncService;

    public PgRagService(
            ChatClient.Builder chatClientBuilder,
            @Qualifier("pgQuestionAnswerAdvisor") QuestionAnswerAdvisor pgQuestionAnswerAdvisor,
            @Qualifier("pgVectorStore") VectorStore pgVectorStore,
            TokenTextSplitter tokenTextSplitter,
            DocumentParseService documentParseService,
            PgDocumentIndexSyncService pgDocumentIndexSyncService) {
        this.chatClient = Objects.requireNonNull(chatClientBuilder, "ChatClient.Builder 未注入").build();
        this.pgQuestionAnswerAdvisor = Objects.requireNonNull(pgQuestionAnswerAdvisor, "pgQuestionAnswerAdvisor 未注入");
        this.pgVectorStore = Objects.requireNonNull(pgVectorStore, "pgVectorStore 未注入");
        this.tokenTextSplitter = Objects.requireNonNull(tokenTextSplitter, "TokenTextSplitter 未注入");
        this.documentParseService = Objects.requireNonNull(documentParseService, "DocumentParseService 未注入");
        this.pgDocumentIndexSyncService = Objects.requireNonNull(pgDocumentIndexSyncService, "PgDocumentIndexSyncService 未注入");
    }

    public RagIngestResponse ingest(RagIngestRequest request) {
        List<Document> documents = request.texts().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(text -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("vectorStore", "postgresql");
                    metadata.put("ingestType", "text");
                    return new Document(text, metadata);
                })
                .toList();
        if (documents.isEmpty()) {
            return new RagIngestResponse(0);
        }
        pgVectorStore.add(documents);
        pgDocumentIndexSyncService.syncDocuments(documents);
        return new RagIngestResponse(documents.size());
    }

    public RagIngestDocumentResponse ingestDocument(MultipartFile file, String title) {
        DocumentParseService.ParsedDocument parsed = documentParseService.parse(file);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("vectorStore", "postgresql");
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

    public RagIngestArticleResponse ingestArticle(RagIngestArticleRequest request) {
        String content = Objects.requireNonNull(request.content(), "content").trim();
        if (content.isEmpty()) {
            return new RagIngestArticleResponse(0, 0);
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("vectorStore", "postgresql");
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

        pgVectorStore.add(chunks);
        pgDocumentIndexSyncService.syncDocuments(chunks);
        return new RagIngestArticleResponse(chunks.size(), content.length());
    }

    public RagAskResponse ask(RagAskRequest request) {
        String question = Objects.requireNonNull(request.question(), "question").trim();
        ChatClientResponse clientResponse = chatClient.prompt()
                .system("你是问答助手。请严格根据随后提供的「上下文片段」作答；若上下文不足以回答，请明确说明并不要编造事实。"
                        + "回答使用中文，简洁有条理。")
                .advisors(pgQuestionAnswerAdvisor)
                .user(question)
                .call()
                .chatClientResponse();

        ChatResponse chatResponse = Objects.requireNonNull(clientResponse.chatResponse(), "chatResponse 为空");
        String answer = Objects.requireNonNull(
                Objects.requireNonNull(chatResponse.getResult(), "生成结果为空").getOutput().getText(),
                "模型返回为空");
        return new RagAskResponse(answer, extractSources(clientResponse));
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
