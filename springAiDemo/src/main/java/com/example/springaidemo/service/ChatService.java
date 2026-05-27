package com.example.springaidemo.service;

import com.example.springaidemo.request.ChatRequest;
import com.example.springaidemo.response.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 封装基于 {@link ChatClient} 的对话调用逻辑。
 */
@Service
public class ChatService {

    /** 由 Spring 注入的 Builder 在构造函数中只 build 一次，避免每次请求重复构建 */
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = Objects.requireNonNull(chatClientBuilder, "ChatClient.Builder 未注入").build();
    }

    /**
     * 发送用户消息，返回模型回复（同步、非流式）。
     * 已通过 {@link org.springframework.ai.chat.client.ChatClientCustomizer} 接入高德 MCP，可回答天气等问题。
     */
    public ChatResponse chat(ChatRequest request) {
        String reply = Objects.requireNonNull(
                chatClient.prompt()
                        .user(Objects.requireNonNull(request.message(), "message"))
                        .call()
                        .content(),
                "模型返回为空");
        return new ChatResponse(reply);
    }
}
