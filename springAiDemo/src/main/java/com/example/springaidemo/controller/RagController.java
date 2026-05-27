package com.example.springaidemo.controller;

import com.example.springaidemo.request.RagAskRequest;
import com.example.springaidemo.request.RagIngestArticleRequest;
import com.example.springaidemo.request.RagIngestRequest;
import com.example.springaidemo.response.RagAskResponse;
import com.example.springaidemo.response.RagIngestArticleResponse;
import com.example.springaidemo.response.RagIngestDocumentResponse;
import com.example.springaidemo.response.RagIngestResponse;
import com.example.springaidemo.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 基于 Chroma {@link org.springframework.ai.vectorstore.VectorStore} 的 RAG HTTP 接口：先入库文本，再按问题检索并生成回答。
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 将多段文本写入向量库；后续 {@link #ask(RagAskRequest)} 可基于这些内容回答。
     */
    @PostMapping(value = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagIngestResponse ingest(@Valid @RequestBody RagIngestRequest request) {
        return ragService.ingest(request);
    }

    /**
     * 传入整篇长文，服务端按 token 自动分块后写入向量库。
     */
    @PostMapping(value = "/documents/article", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagIngestArticleResponse ingestArticle(@Valid @RequestBody RagIngestArticleRequest request) {
        return ragService.ingestArticle(request);
    }

    /**
     * 上传 PDF / Word / TXT / Markdown，解析并分块后写入向量库。
     */
    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagIngestDocumentResponse ingestDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        return ragService.ingestDocument(file, title);
    }

    /**
     * RAG 问答：检索相关片段，由大模型结合上下文生成回答，并返回引用摘要。
     */
    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagAskResponse ask(@Valid @RequestBody RagAskRequest request) {
        return ragService.ask(request);
    }
}
