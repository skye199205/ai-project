package com.example.springaidemo.service.keyword;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * 内存倒排索引，用于关键词召回（与向量库并行，便于对比学习）。
 */
@Component
public class KeywordDocumentIndex {

    private static final Pattern TOKEN_SPLIT = Pattern.compile("[\\s\\p{Punct}]+");
    private static final Pattern CJK = Pattern.compile("\\p{Script=Han}");

    private final CopyOnWriteArrayList<IndexedEntry> entries = new CopyOnWriteArrayList<>();

    /**
     * 将文档片段写入关键词索引。
     */
    public void addAll(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        for (Document document : documents) {
            String text = document.getText();
            if (!StringUtils.hasText(text)) {
                continue;
            }
            String docId = resolveDocumentId(document);
            entries.add(new IndexedEntry(
                    docId,
                    text,
                    document.getMetadata() != null ? Map.copyOf(document.getMetadata()) : Map.of(),
                    tokenize(text)));
        }
    }

    /**
     * 按查询词 TF-IDF 风格打分，返回 topK 条。
     */
    public List<KeywordSearchResult> search(String query, int topK) {
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty() || entries.isEmpty() || topK <= 0) {
            return List.of();
        }

        int totalDocs = entries.size();
        Map<String, Integer> docFreq = new HashMap<>();
        for (String term : queryTerms) {
            int count = 0;
            for (IndexedEntry entry : entries) {
                if (entry.termFreq().getOrDefault(term, 0) > 0) {
                    count++;
                }
            }
            docFreq.put(term, count);
        }

        List<KeywordSearchResult> ranked = new ArrayList<>();
        for (IndexedEntry entry : entries) {
            double score = 0.0;
            for (String term : queryTerms) {
                int tf = entry.termFreq().getOrDefault(term, 0);
                if (tf == 0) {
                    continue;
                }
                int df = docFreq.getOrDefault(term, 0);
                double idf = Math.log((totalDocs - df + 0.5) / (df + 0.5) + 1.0);
                score += tf * idf;
            }
            if (score > 0) {
                ranked.add(new KeywordSearchResult(entry.documentId(), entry.text(), entry.metadata(), score));
            }
        }

        ranked.sort(Comparator.comparingDouble(KeywordSearchResult::score).reversed());
        if (ranked.size() <= topK) {
            return List.copyOf(ranked);
        }
        return List.copyOf(ranked.subList(0, topK));
    }

    public int size() {
        return entries.size();
    }

    private static String resolveDocumentId(Document document) {
        if (StringUtils.hasText(document.getId())) {
            return document.getId();
        }
        return UUID.randomUUID().toString();
    }

    static List<String> tokenize(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String normalized = text.toLowerCase(Locale.ROOT).trim();
        List<String> tokens = new ArrayList<>();

        for (String part : TOKEN_SPLIT.split(normalized)) {
            if (part.length() >= 2) {
                tokens.add(part);
            }
            if (CJK.matcher(part).find()) {
                for (int i = 0; i < part.length() - 1; i++) {
                    tokens.add(part.substring(i, i + 2));
                }
            }
        }
        return tokens.stream().distinct().toList();
    }

    private record IndexedEntry(
            String documentId,
            String text,
            Map<String, Object> metadata,
            Map<String, Integer> termFreq
    ) {
        IndexedEntry(String documentId, String text, Map<String, Object> metadata, List<String> tokens) {
            this(documentId, text, metadata, toTermFreq(Objects.requireNonNull(tokens)));
        }

        private static Map<String, Integer> toTermFreq(List<String> tokens) {
            Map<String, Integer> freq = new HashMap<>();
            for (String token : tokens) {
                freq.merge(token, 1, Integer::sum);
            }
            return freq;
        }
    }
}
