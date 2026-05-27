package com.example.springaidemo.response;

import java.util.List;

/**
 * RAG 问答响应：模型答案 + 参与增强的检索片段摘要。
 */
public record RagAskResponse(String answer, List<RagSource> sources) {
}
