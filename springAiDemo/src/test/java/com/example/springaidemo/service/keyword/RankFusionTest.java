package com.example.springaidemo.service.keyword;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RankFusionTest {

    @Test
    void reciprocalRankFusion_shouldMergeTwoPaths() {
        List<Document> vectorHits = List.of(
                new Document("doc-a", "向量命中 A", java.util.Map.of()),
                new Document("doc-b", "向量命中 B", java.util.Map.of()));

        List<KeywordSearchResult> keywordHits = List.of(
                new KeywordSearchResult("doc-b", "关键词命中 B", java.util.Map.of(), 2.0),
                new KeywordSearchResult("doc-c", "关键词命中 C", java.util.Map.of(), 1.5));

        List<RankFusion.FusedChunk> fused = RankFusion.reciprocalRankFusion(vectorHits, keywordHits, 3, 60);

        assertThat(fused).hasSize(3);
        assertThat(fused.getFirst().text()).contains("B");
        assertThat(fused.stream().filter(c -> c.recallPath() == RecallPath.FUSION).count()).isGreaterThanOrEqualTo(1);
    }
}
