package com.example.springaidemo.response;

/**
 * 混合召回各路的命中数量，便于与单路 QA Advisor 对比。
 */
public record RagRecallStats(
        int vectorHitCount,
        int keywordHitCount,
        int fusedCount,
        int keywordIndexSize
) {
}
