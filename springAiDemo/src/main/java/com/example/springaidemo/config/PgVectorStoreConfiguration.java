package com.example.springaidemo.config;

import com.example.springaidemo.service.keyword.PgKeywordDocumentIndex;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Objects;

/**
 * PostgreSQL pgvector 向量库配置（独立 Bean，不影响 Chroma 默认 {@link VectorStore}）。
 */
@Configuration
@ConditionalOnProperty(prefix = "app.pgvector", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PgVectorStoreConfiguration {

    @Bean("pgVectorStore")
    VectorStore pgVectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.rag.pgvector.dimensions:1024}") int dimensions,
            @Value("${spring.ai.rag.pgvector.table-name:spring_ai_demo_pg_store}") String tableName) {
        return PgVectorStore.builder(jdbcTemplate, Objects.requireNonNull(embeddingModel, "EmbeddingModel 未注入"))
                .vectorTableName(tableName)
                .dimensions(dimensions)
                .initializeSchema(true)
                .build();
    }

    @Bean
    PgKeywordDocumentIndex pgKeywordDocumentIndex() {
        return new PgKeywordDocumentIndex();
    }

    @Bean("pgQuestionAnswerAdvisor")
    QuestionAnswerAdvisor pgQuestionAnswerAdvisor(
            @Qualifier("pgVectorStore") VectorStore pgVectorStore,
            @Value("${spring.ai.rag.top-k:4}") int topK,
            @Value("${spring.ai.rag.similarity-threshold:0.6}") double similarityThreshold) {
        return QuestionAnswerAdvisor.builder(Objects.requireNonNull(pgVectorStore, "pgVectorStore 未注入"))
                .searchRequest(SearchRequest.builder()
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .build())
                .build();
    }
}
