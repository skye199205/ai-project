package com.example.springaidemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * 默认上下文测试不拉起 Chroma，改为内存 {@link org.springframework.ai.vectorstore.SimpleVectorStore}。
 */
@SpringBootTest(
        properties = "spring.autoconfigure.exclude="
                + "org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration")
@Import(TestInMemoryVectorStoreConfiguration.class)
class SpringAiDemoApplicationTests {

    @Test
    void contextLoads() {
    }

}
