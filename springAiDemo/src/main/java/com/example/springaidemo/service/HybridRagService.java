package com.example.springaidemo.service;

import com.example.springaidemo.request.RagAskRequest;
import com.example.springaidemo.response.RagHybridAskResponse;
import com.example.springaidemo.response.RagHybridSource;
import com.example.springaidemo.response.RagRecallStats;
import com.example.springaidemo.service.keyword.KeywordDocumentIndex;
import com.example.springaidemo.service.keyword.KeywordSearchResult;
import com.example.springaidemo.service.keyword.RankFusion;
import com.example.springaidemo.service.keyword.RankFusion.FusedChunk;
import com.example.springaidemo.service.keyword.RecallPath;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 混合 RAG：向量语义召回 + 关键词召回，经 RRF 融合后再交给大模型生成答案。
 * 与 {@link RagService#ask}（QuestionAnswerAdvisor 单路）并列，便于对比学习。
 */
@Service
public class HybridRagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final KeywordDocumentIndex keywordDocumentIndex;

    private final int vectorTopK;
    private final int keywordTopK;
    private final int finalTopK;
    private final double similarityThreshold;
    private final int rrfK;

    public HybridRagService(
            ChatClient.Builder chatClientBuilder,
            VectorStore vectorStore,
            KeywordDocumentIndex keywordDocumentIndex,
            @Value("${spring.ai.rag.hybrid.vector-top-k:8}") int vectorTopK,
            @Value("${spring.ai.rag.hybrid.keyword-top-k:8}") int keywordTopK,
            @Value("${spring.ai.rag.hybrid.final-top-k:6}") int finalTopK,
            @Value("${spring.ai.rag.hybrid.similarity-threshold:0.5}") double similarityThreshold,
            @Value("${spring.ai.rag.hybrid.rrf-k:60}") int rrfK) {
        this.chatClient = Objects.requireNonNull(chatClientBuilder, "ChatClient.Builder 未注入").build();
        this.vectorStore = Objects.requireNonNull(vectorStore, "VectorStore 未注入");
        this.keywordDocumentIndex = Objects.requireNonNull(keywordDocumentIndex, "KeywordDocumentIndex 未注入");
        this.vectorTopK = vectorTopK;
        this.keywordTopK = keywordTopK;
        this.finalTopK = finalTopK;
        this.similarityThreshold = similarityThreshold;
        this.rrfK = rrfK;
    }

    /**
     * 多路召回问答：向量路 + 关键词路 → RRF 融合 → 拼装上下文 → 大模型作答。
     */
    public RagHybridAskResponse askHybrid(RagAskRequest request) {
        String question = Objects.requireNonNull(request.question(), "question").trim();

        List<Document> vectorHits = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(vectorTopK)
                        .similarityThreshold(similarityThreshold)
                        .build());

        List<KeywordSearchResult> keywordHits = keywordDocumentIndex.search(question, keywordTopK);

        List<FusedChunk> fused = RankFusion.reciprocalRankFusion(
                vectorHits, keywordHits, finalTopK, rrfK);

        String context = buildContext(fused);
        String answer = generateAnswer(question, context);

        return new RagHybridAskResponse(
                answer,
                toSources(fused),
                new RagRecallStats(
                        vectorHits.size(),
                        keywordHits.size(),
                        fused.size(),
                        keywordDocumentIndex.size()));
    }

    private String buildContext(List<FusedChunk> fused) {
        if (fused.isEmpty()) {
            return "（未检索到相关片段，请说明知识库中暂无可用上下文。）";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fused.size(); i++) {
            FusedChunk chunk = fused.get(i);
            sb.append("【片段 ").append(i + 1).append("｜来源 ").append(chunk.recallPath()).append("】\n");
            sb.append(chunk.text()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private String generateAnswer(String question, String context) {
        ChatResponse chatResponse = chatClient.prompt()
                .system("你是问答助手。请严格根据用户提供的「上下文片段」作答；若上下文不足以回答，请明确说明，不要编造。"
                        + "回答使用中文，简洁有条理。")
                .user("上下文片段：\n" + context + "\n\n用户问题：\n" + question)
                .call()
                .chatResponse();

        return Objects.requireNonNull(
                Objects.requireNonNull(chatResponse.getResult(), "生成结果为空").getOutput().getText(),
                "模型返回为空");
    }

    private static List<RagHybridSource> toSources(List<FusedChunk> fused) {
        List<RagHybridSource> sources = new ArrayList<>();
        for (FusedChunk chunk : fused) {
            String text = chunk.text();
            String excerpt = text.length() > 500 ? text.substring(0, 500) + "…" : text;
            sources.add(new RagHybridSource(
                    excerpt,
                    chunk.fusionScore(),
                    chunk.recallPath().name()));
        }
        return List.copyOf(sources);
    }
}
