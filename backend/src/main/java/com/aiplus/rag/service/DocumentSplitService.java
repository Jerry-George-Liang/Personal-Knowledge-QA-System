package com.aiplus.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentSplitService {

    private static final Logger log = LoggerFactory.getLogger(DocumentSplitService.class);

    @Value("${rag.splitting.max-segment-size:1000}")
    private int maxSegmentSize;

    @Value("${rag.splitting.overlap:200}")
    private int overlapSize;

    public List<TextSegment> split(List<Document> documents, String fileName) {
        List<TextSegment> segments = new ArrayList<>();
        int globalIndex = 0;

        for (Document doc : documents) {
            String text = doc.text();
            if (text == null || text.isBlank()) continue;

            if (text.length() <= maxSegmentSize) {
                Metadata meta = new Metadata();
                meta.put("segment_index", String.valueOf(globalIndex));
                meta.put("source_file", fileName);
                segments.add(TextSegment.from(text, meta));
                globalIndex++;
                continue;
            }

            List<String> chunks = splitByFixedSize(text);
            for (String chunk : chunks) {
                Metadata meta = new Metadata();
                meta.put("segment_index", String.valueOf(globalIndex++));
                meta.put("source_file", fileName);
                segments.add(TextSegment.from(chunk.trim(), meta));
            }
        }

        log.info("文档切分完成: {} -> {} 个段落", documents.size(), segments.size());
        return segments;
    }

    private List<String> splitByFixedSize(String text) {
        List<String> chunks = new ArrayList<>();
        if (text.length() <= maxSegmentSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxSegmentSize, text.length());
            if (end < text.length()) {
                int breakPoint = findBreakPoint(text, end);
                end = breakPoint > start ? breakPoint : end;
            }
            chunks.add(text.substring(start, end).trim());
            start = end - overlapSize;
            if (start < 0) start = 0;
            if (start >= text.length()) break;
        }
        return chunks;
    }

    private int findBreakPoint(String text, int preferredEnd) {
        for (int i = preferredEnd; i > Math.max(preferredEnd - overlapSize, 0); i--) {
            char c = text.charAt(i);
            if (c == '\n' || c == '。' || c == '！' || c == '？' || c == '；') {
                return i + 1;
            }
        }
        return preferredEnd;
    }
}
