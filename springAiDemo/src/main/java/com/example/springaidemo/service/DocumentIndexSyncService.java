package com.example.springaidemo.service;

import com.example.springaidemo.service.keyword.KeywordDocumentIndex;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 入库时同步维护关键词索引，供混合召回使用；不改变原有入库接口的返回结构。
 */
@Service
public class DocumentIndexSyncService {

    private final KeywordDocumentIndex keywordDocumentIndex;

    public DocumentIndexSyncService(KeywordDocumentIndex keywordDocumentIndex) {
        this.keywordDocumentIndex = Objects.requireNonNull(keywordDocumentIndex, "KeywordDocumentIndex 未注入");
    }

    /**
     * 将写入向量库的同一批文档同步进关键词索引。
     */
    public void syncDocuments(List<Document> documents) {
        keywordDocumentIndex.addAll(documents);
    }
}
