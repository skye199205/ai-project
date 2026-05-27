package com.example.springaidemo.response;

/**
 * 单条检索到的引用片段（供前端展示或审计）。
 */
public record RagSource(String excerpt, Double score) {
}
