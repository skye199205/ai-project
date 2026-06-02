package com.example.springaidemo.service.keyword;

import org.springframework.ai.document.Document;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 多路召回结果融合（RRF，Reciprocal Rank Fusion）。
 */
public final class RankFusion {

    private RankFusion() {
    }

    /**
     * 将向量路与关键词路按 RRF 合并，返回按融合分排序的片段列表。
     */
    public static List<FusedChunk> reciprocalRankFusion(
            List<Document> vectorHits,
            List<KeywordSearchResult> keywordHits,
            int finalTopK,
            int rrfK) {

        Map<String, MutableFused> pool = new LinkedHashMap<>();

        for (int rank = 0; rank < vectorHits.size(); rank++) {
            Document doc = vectorHits.get(rank);
            String key = fusionKey(doc.getId(), doc.getText());
            pool.computeIfAbsent(key, k -> new MutableFused(doc.getText(), doc.getMetadata()))
                    .addVectorScore(1.0 / (rrfK + rank + 1));
        }

        for (int rank = 0; rank < keywordHits.size(); rank++) {
            KeywordSearchResult hit = keywordHits.get(rank);
            String key = fusionKey(hit.documentId(), hit.text());
            pool.computeIfAbsent(key, k -> new MutableFused(hit.text(), hit.metadata()))
                    .addKeywordScore(1.0 / (rrfK + rank + 1), hit.score());
        }

        List<FusedChunk> fused = new ArrayList<>();
        for (MutableFused item : pool.values()) {
            fused.add(item.toChunk());
        }
        fused.sort(Comparator.comparingDouble(FusedChunk::fusionScore).reversed());
        if (fused.size() <= finalTopK) {
            return List.copyOf(fused);
        }
        return List.copyOf(fused.subList(0, finalTopK));
    }

    private static String fusionKey(String documentId, String text) {
        if (StringUtils.hasText(documentId)) {
            return "id:" + documentId;
        }
        return "text:" + Objects.hash(text != null ? text : "");
    }

    public record FusedChunk(
            String text,
            Map<String, Object> metadata,
            double fusionScore,
            RecallPath recallPath,
            Double vectorRankScore,
            Double keywordScore
    ) {
    }

    private static final class MutableFused {

        private final String text;
        private final Map<String, Object> metadata;
        private double fusionScore;
        private boolean fromVector;
        private boolean fromKeyword;
        private Double keywordScore;

        private MutableFused(String text, Map<String, Object> metadata) {
            this.text = text != null ? text : "";
            this.metadata = metadata != null ? metadata : Map.of();
        }

        void addVectorScore(double contribution) {
            fusionScore += contribution;
            fromVector = true;
        }

        void addKeywordScore(double contribution, double rawKeywordScore) {
            fusionScore += contribution;
            fromKeyword = true;
            keywordScore = rawKeywordScore;
        }

        FusedChunk toChunk() {
            RecallPath path;
            if (fromVector && fromKeyword) {
                path = RecallPath.FUSION;
            } else if (fromVector) {
                path = RecallPath.VECTOR;
            } else {
                path = RecallPath.KEYWORD;
            }
            return new FusedChunk(text, metadata, fusionScore, path, null, keywordScore);
        }
    }
}
