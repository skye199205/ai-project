package com.example.springaidemo.service.keyword;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordDocumentIndexTest {

    private KeywordDocumentIndex index;

    @BeforeEach
    void setUp() {
        index = new KeywordDocumentIndex();
    }

    @Test
    void search_shouldRankByKeywordRelevance() {
        index.addAll(List.of(
                new Document("Spring AI 使用 Chroma 作为向量数据库。"),
                new Document("今天北京天气晴朗，适合出行。")));

        List<KeywordSearchResult> hits = index.search("Chroma 向量库", 2);

        assertThat(hits).isNotEmpty();
        assertThat(hits.getFirst().text()).contains("Chroma");
    }

    @Test
    void tokenize_shouldSupportChineseBigram() {
        List<String> tokens = KeywordDocumentIndex.tokenize("向量检索");

        assertThat(tokens).isNotEmpty();
    }
}
