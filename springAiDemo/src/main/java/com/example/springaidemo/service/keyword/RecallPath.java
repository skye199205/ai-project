package com.example.springaidemo.service.keyword;

/**
 * 多路召回来源，便于与单路 {@code /api/rag/ask} 对比。
 */
public enum RecallPath {
    /** 向量语义检索 */
    VECTOR,
    /** 关键词倒排检索 */
    KEYWORD,
    /** 多路 RRF 融合后入选 */
    FUSION
}
