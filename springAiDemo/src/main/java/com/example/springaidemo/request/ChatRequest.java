package com.example.springaidemo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 聊天请求体：用户输入的对话内容。
 */
public record ChatRequest(

        @NotBlank(message = "message 不能为空")
        @Size(max = 8000, message = "message 长度不能超过 8000 字符")
        String message
) {
}
