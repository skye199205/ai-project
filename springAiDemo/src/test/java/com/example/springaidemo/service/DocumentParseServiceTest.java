package com.example.springaidemo.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentParseServiceTest {

    private final DocumentParseService documentParseService = new DocumentParseService();

    @Test
    void parseTxtFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "note.txt",
                "text/plain",
                "第一段\n\n第二段".getBytes());

        DocumentParseService.ParsedDocument parsed = documentParseService.parse(file);

        assertThat(parsed.fileName()).isEqualTo("note.txt");
        assertThat(parsed.fileType()).isEqualTo("txt");
        assertThat(parsed.content()).contains("第一段").contains("第二段");
    }

    @Test
    void rejectUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "ignored".getBytes());

        assertThatThrownBy(() -> documentParseService.parse(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的文件格式");
    }
}
