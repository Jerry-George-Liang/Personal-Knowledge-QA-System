package com.aiplus.rag.model;

import dev.langchain4j.data.document.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentParseResult {

    private String documentId;
    private String fileName;
    private int segmentCount;
    private long elapsedMs;
    private List<Document> documents;
}
