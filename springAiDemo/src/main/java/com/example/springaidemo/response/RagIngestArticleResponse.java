package com.example.springaidemo.response;

/**
 * 长文分块入库结果。
 */
public record RagIngestArticleResponse(int chunkCount, int contentLength) {
}
