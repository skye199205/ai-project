package com.example.springaidemo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 调用 DashScope（经 OpenAI 兼容接口）聊天模型的集成测试。
 * 需要能访问外网，且已配置 API Key（例如 src/main/resources/application-local.yaml）。
 * 若出现 HTTP 404，请核对 base-url（地域）与模型名是否在控制台可用。
 * 本测试排除了 Chroma 自动配置并使用内存向量库，避免必须单独启动 Chroma 进程。
 */
@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration")
@Import(TestInMemoryVectorStoreConfiguration.class)
@Tag("live-api")
class DashScopeChatIntegrationTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    @DisplayName("ChatClient 发起对话并能拿到非空回复")
    void chatClientReturnsNonEmptyContent() {
        String reply = ChatClient.create(Objects.requireNonNull(chatModel, "ChatModel 未注入"))
                .prompt()
                .user("用不超过 10 个字介绍你自己。")
                .call()
                .content();

        assertThat(reply).isNotBlank();
    }

    @Test
    @DisplayName("ChatModel 直接调用并能拿到非空回复")
    void chatModelCallReturnsNonEmptyOutput() {
        String reply = ChatClient.create(Objects.requireNonNull(chatModel, "ChatModel 未注入"))
                .prompt("1+1 等于几？只回答数字。")
                .call()
                .content();

        assertThat(reply).isNotBlank();
    }
}
