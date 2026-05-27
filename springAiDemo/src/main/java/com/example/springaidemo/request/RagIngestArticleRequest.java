package com.example.springaidemo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 整篇长文入库请求：由服务端自动分块后再写入向量库。
 */
public record RagIngestArticleRequest(
        @NotBlank(message = "content 不能为空")
        @Size(max = 500_000, message = "content 长度不能超过 500000 字符")
        String content,

        @Size(max = 200, message = "title 长度不能超过 200 字符")
        String title
) {
}
