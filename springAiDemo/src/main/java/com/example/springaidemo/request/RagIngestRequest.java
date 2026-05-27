package com.example.springaidemo.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 向内存向量库写入的文本段落列表（每段会单独建嵌入并可供检索）。
 */
public record RagIngestRequest(
        @NotEmpty(message = "texts 不能为空")
        @Size(max = 50, message = "单次最多提交 50 段文本")
        List<@Size(max = 8000, message = "单段文本不超过 8000 字符") String> texts
) {
}
