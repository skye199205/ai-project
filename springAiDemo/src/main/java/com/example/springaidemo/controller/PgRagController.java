package com.example.springaidemo.controller;

import com.example.springaidemo.request.RagAskRequest;
import com.example.springaidemo.request.RagIngestArticleRequest;
import com.example.springaidemo.request.RagIngestRequest;
import com.example.springaidemo.response.RagAskResponse;
import com.example.springaidemo.response.RagHybridAskResponse;
import com.example.springaidemo.response.RagIngestArticleResponse;
import com.example.springaidemo.response.RagIngestDocumentResponse;
import com.example.springaidemo.response.RagIngestResponse;
import com.example.springaidemo.service.PgHybridRagService;
import com.example.springaidemo.service.PgRagService;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 基于 PostgreSQL pgvector 的 RAG HTTP 接口（与 {@link RagController} Chroma 路并存，便于对比）。
 */
@RestController
@RequestMapping("/api/pg-rag")
@ConditionalOnBean(name = "pgVectorStore")
public class PgRagController {

    private final PgRagService pgRagService;
    private final PgHybridRagService pgHybridRagService;

    public PgRagController(PgRagService pgRagService, PgHybridRagService pgHybridRagService) {
        this.pgRagService = pgRagService;
        this.pgHybridRagService = pgHybridRagService;
    }

    /**
     * 多段文本写入 PostgreSQL 向量表。
     */
    @PostMapping(value = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagIngestResponse ingest(@Valid @RequestBody RagIngestRequest request) {
        return pgRagService.ingest(request);
    }

    /**
     * 长文自动分块后写入 PostgreSQL 向量表。
     */
    @PostMapping(value = "/documents/article", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagIngestArticleResponse ingestArticle(@Valid @RequestBody RagIngestArticleRequest request) {
        return pgRagService.ingestArticle(request);
    }

    /**
     * 上传文档解析后写入 PostgreSQL 向量表。
     */
    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagIngestDocumentResponse ingestDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        return pgRagService.ingestDocument(file, title);
    }

    /**
     * 单路向量 RAG 问答（pgvector + QuestionAnswerAdvisor）。
     */
    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagAskResponse ask(@Valid @RequestBody RagAskRequest request) {
        return pgRagService.ask(request);
    }

    /**
     * 混合 RAG 问答（pgvector + 关键词 + RRF）。
     */
    @PostMapping(value = "/ask/hybrid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RagHybridAskResponse askHybrid(@Valid @RequestBody RagAskRequest request) {
        return pgHybridRagService.askHybrid(request);
    }
}
