package com.aiplus.rag.parser.impl;

import com.aiplus.rag.exception.FileParseException;
import com.aiplus.rag.parser.FileParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TextFileParser implements FileParser {

    @Override
    public List<Document> parse(InputStream inputStream, String fileName) throws Exception {
        List<Document> documents = new ArrayList<>();
        String sourceType = isMarkdown(fileName) ? "markdown" : "text";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder segmentBuilder = new StringBuilder();
            String line;
            int segmentIndex = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (segmentBuilder.length() > 0) {
                        addSegment(documents, segmentBuilder.toString(), fileName, sourceType, segmentIndex++);
                        segmentBuilder.setLength(0);
                    }
                } else {
                    if (segmentBuilder.length() > 0) {
                        segmentBuilder.append("\n");
                    }
                    segmentBuilder.append(line);
                }
            }

            if (segmentBuilder.length() > 0) {
                addSegment(documents, segmentBuilder.toString(), fileName, sourceType, segmentIndex);
            }
        } catch (IOException e) {
            throw new FileParseException(fileName, "文本文件读取错误: " + e.getMessage(), e);
        }
        return documents;
    }

    private void addSegment(List<Document> documents, String content, String fileName,
                            String sourceType, int index) {
        Metadata metadata = new Metadata();
        metadata.put("file_name", fileName);
        metadata.put("source_type", sourceType);
        metadata.put("segment_index", String.valueOf(index));
        documents.add(new Document(content.trim(), metadata));
    }

    private boolean isMarkdown(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".md") || lowerName.endsWith(".markdown");
    }

    @Override
    public boolean supports(String fileExtension) {
        return "txt".equalsIgnoreCase(fileExtension)
                || "md".equalsIgnoreCase(fileExtension)
                || "markdown".equalsIgnoreCase(fileExtension);
    }
}
