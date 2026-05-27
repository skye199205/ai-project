package com.example.springaidemo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RAG 问答请求：根据已入库的向量片段回答问题。
 */
public record RagAskRequest(
        @NotBlank(message = "question 不能为空")
        @Size(max = 4000, message = "question 长度不能超过 4000 字符")
        String question
) {
}
