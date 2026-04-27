package com.aiplus.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadata {

    private String documentId;
    private String fileName;
    private LocalDateTime uploadTime;
    private int segmentCount;
}
