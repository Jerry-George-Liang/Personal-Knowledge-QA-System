package com.aiplus.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {

    private String sessionId;
    private long createdAt;
    private int messageCount;
    private String title;
}
