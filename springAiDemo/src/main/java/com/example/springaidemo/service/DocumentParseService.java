package com.example.springaidemo.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 从上传文件中抽取纯文本，供 RAG 分块入库。
 */
@Service
public class DocumentParseService {

    /** 允许上传的扩展名（小写，不含点） */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt", "md", "markdown");

    private final Tika tika = new Tika();

    /**
     * 解析上传文件为 UTF-8 文本。
     *
     * @throws IllegalArgumentException 格式不支持或正文为空
     */
    public ParsedDocument parse(MultipartFile file) {
        Objects.requireNonNull(file, "file 不能为空");
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String fileName = StringUtils.hasText(originalFilename) ? originalFilename.trim() : "unknown";
        String extension = extractExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件格式：" + extension + "，仅支持 PDF、Word（doc/docx）、TXT、Markdown");
        }

        String text;
        try (InputStream inputStream = file.getInputStream()) {
            text = tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            throw new IllegalArgumentException("文档解析失败：" + e.getMessage(), e);
        }

        if (text == null) {
            text = "";
        }
        text = text.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("未能从文档中抽取有效文本，请检查文件是否为空或受密码保护");
        }

        return new ParsedDocument(fileName, extension, text);
    }

    private static String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 解析后的文档正文。
     */
    public record ParsedDocument(String fileName, String fileType, String content) {
    }
}
