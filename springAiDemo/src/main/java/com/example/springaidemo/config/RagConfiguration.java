package com.example.springaidemo.config;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * RAG Advisor 与文档分块。向量库存储由 Spring AI Chroma 自动配置提供 {@link VectorStore} bean。
 */
@Configuration
public class RagConfiguration {

        /** 长文自动分块（按 token 切分，适合中英文混合文本） */
        /**
         * 
         * @param chunkSize         每块知识大约 800 token，不大不小
         * @param minChunkSizeChars 小于 350 字符的碎片不要，避免垃圾
         *                          withKeepSeparator eepSeparator true：切的时候保留段落，不把句子撕烂
         * @return
         */
        @Bean
        TokenTextSplitter ragTokenTextSplitter(
                        @Value("${spring.ai.rag.chunk-size:800}") int chunkSize,
                        @Value("${spring.ai.rag.min-chunk-size-chars:350}") int minChunkSizeChars) {
                return TokenTextSplitter.builder()
                                .withChunkSize(chunkSize)
                                .withMinChunkSizeChars(minChunkSizeChars)
                                .withKeepSeparator(true)
                                .build();
        }

        /**
         * 
         * @param vectorStore
         * @param topK                最多返回 4 条最相关的知识片段 企业常用：3～5
         * @param similarityThreshold 相似度 ≥ 0.6 才会被返回
         * @return
         */
        @Bean
        QuestionAnswerAdvisor questionAnswerAdvisor(
                        VectorStore vectorStore,
                        @Value("${spring.ai.rag.top-k:4}") int topK,
                        @Value("${spring.ai.rag.similarity-threshold:0.6}") double similarityThreshold) {
                return QuestionAnswerAdvisor.builder(Objects.requireNonNull(vectorStore, "VectorStore 未注入"))
                                .searchRequest(SearchRequest.builder()
                                                .topK(topK)
                                                .similarityThreshold(similarityThreshold)
                                                .build())
                                .build();
        }
}
