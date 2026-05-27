package com.example.springaidemo.response;

/**
 * 文档上传入库结果：解析正文后分块写入向量库。
 */
public record RagIngestDocumentResponse(
        String fileName,
        String fileType,
        int chunkCount,
        int contentLength
) {
}
