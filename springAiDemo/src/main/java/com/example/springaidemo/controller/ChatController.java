package com.example.springaidemo.controller;

import com.example.springaidemo.request.ChatRequest;
import com.example.springaidemo.response.ChatResponse;
import com.example.springaidemo.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基于 Spring AI 的简单聊天 HTTP 接口。
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 发送用户消息，返回模型回复（同步、非流式）。
     * 已通过 {@link org.springframework.ai.chat.client.ChatClientCustomizer} 接入高德 MCP，可回答天气等问题。
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
}
