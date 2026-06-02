package com.example.springaidemo.response;

import java.util.List;

/**
 * 多路召回 + 关键词检索的 RAG 问答响应（对比学习用）。
 */
public record RagHybridAskResponse(
        String answer,
        List<RagHybridSource> sources,
        RagRecallStats recallStats
) {
}
