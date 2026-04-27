package com.aiplus.rag.parser.impl;

import com.aiplus.rag.exception.FileParseException;
import com.aiplus.rag.parser.FileParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfFileParser implements FileParser {

    @Override
    public List<Document> parse(InputStream inputStream, String fileName) throws Exception {
        List<Document> documents = new ArrayList<>();
        try (PDDocument pdfDocument = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            int totalPages = pdfDocument.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(pdfDocument);

                if (pageText != null && !pageText.isBlank()) {
                    String trimmedText = pageText.trim();
                    Metadata metadata = new Metadata();
                    metadata.put("file_name", fileName);
                    metadata.put("source_type", "pdf");
                    metadata.put("page_number", String.valueOf(page));
                    metadata.put("total_pages", String.valueOf(totalPages));

                    documents.add(new Document(trimmedText, metadata));
                }
            }
        } catch (Exception e) {
            throw new FileParseException(fileName, "PDF文件解析错误: " + e.getMessage(), e);
        }
        return documents;
    }

    @Override
    public boolean supports(String fileExtension) {
        return "pdf".equalsIgnoreCase(fileExtension);
    }
}
