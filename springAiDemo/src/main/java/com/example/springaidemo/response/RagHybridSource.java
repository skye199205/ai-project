package com.example.springaidemo.response;

/**
 * 混合召回引用片段，标注来源路径。
 */
public record RagHybridSource(
        String excerpt,
        Double score,
        String recallPath
) {
}
