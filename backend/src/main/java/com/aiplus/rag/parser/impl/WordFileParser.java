package com.aiplus.rag.parser.impl;

import com.aiplus.rag.exception.FileParseException;
import com.aiplus.rag.parser.FileParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class WordFileParser implements FileParser {

    @Override
    public List<Document> parse(InputStream inputStream, String fileName) throws Exception {
        List<Document> documents = new ArrayList<>();
        try (XWPFDocument doc = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            StringBuilder currentContent = new StringBuilder();
            int paragraphIndex = 0;

            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText().trim();

                if (text.isEmpty() && currentContent.length() > 0) {
                    addDocument(documents, currentContent.toString(), fileName, paragraphIndex++);
                    currentContent.setLength(0);
                    continue;
                }

                if (!text.isEmpty()) {
                    if (currentContent.length() > 0) {
                        currentContent.append("\n");
                    }
                    currentContent.append(text);
                }
            }

            if (currentContent.length() > 0) {
                addDocument(documents, currentContent.toString(), fileName, paragraphIndex);
            }

            for (var table : doc.getTables()) {
                StringBuilder tableContent = new StringBuilder("表格内容:\n");
                for (var row : table.getRows()) {
                    List<String> cells = new ArrayList<>();
                    for (var cell : row.getTableCells()) {
                        cells.add(cell.getText().trim());
                    }
                    tableContent.append(String.join(" | ", cells)).append("\n");
                }
                String tableStr = tableContent.toString().trim();
                if (!tableStr.isEmpty()) {
                    Metadata metadata = new Metadata();
                    metadata.put("file_name", fileName);
                    metadata.put("source_type", "docx");
                    metadata.put("content_type", "table");
                    documents.add(new Document(tableStr, metadata));
                }
            }
        } catch (Exception e) {
            throw new FileParseException(fileName, "Word文件解析错误: " + e.getMessage(), e);
        }
        return documents;
    }

    private void addDocument(List<Document> documents, String content, String fileName, int index) {
        Metadata metadata = new Metadata();
        metadata.put("file_name", fileName);
        metadata.put("source_type", "docx");
        metadata.put("paragraph_index", String.valueOf(index));
        documents.add(new Document(content.trim(), metadata));
    }

    @Override
    public boolean supports(String fileExtension) {
        return "docx".equalsIgnoreCase(fileExtension);
    }
}
