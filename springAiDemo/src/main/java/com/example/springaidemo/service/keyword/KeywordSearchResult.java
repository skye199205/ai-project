package com.example.springaidemo.service.keyword;

import java.util.Map;

/**
 * 关键词检索单条命中结果。
 */
public record KeywordSearchResult(
        String documentId,
        String text,
        Map<String, Object> metadata,
        double score
) {
}
