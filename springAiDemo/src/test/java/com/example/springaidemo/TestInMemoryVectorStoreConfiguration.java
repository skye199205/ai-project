package com.example.springaidemo;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

/**
 * 单元测试上下文：不使用真实 Chroma 服务时使用内存向量库，避免上下文启动失败。
 */
@TestConfiguration
public class TestInMemoryVectorStoreConfiguration {

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(Objects.requireNonNull(embeddingModel, "EmbeddingModel 未注入")).build();
    }
}
