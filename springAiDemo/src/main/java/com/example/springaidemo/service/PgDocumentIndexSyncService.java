package com.example.springaidemo.service;

import com.example.springaidemo.service.keyword.PgKeywordDocumentIndex;
import org.springframework.ai.document.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * PostgreSQL 路入库时同步维护关键词索引。
 */
@Service
@ConditionalOnBean(name = "pgVectorStore")
public class PgDocumentIndexSyncService {

    private final PgKeywordDocumentIndex pgKeywordDocumentIndex;

    public PgDocumentIndexSyncService(PgKeywordDocumentIndex pgKeywordDocumentIndex) {
        this.pgKeywordDocumentIndex = Objects.requireNonNull(pgKeywordDocumentIndex, "PgKeywordDocumentIndex 未注入");
    }

    public void syncDocuments(List<Document> documents) {
        pgKeywordDocumentIndex.addAll(documents);
    }
}
