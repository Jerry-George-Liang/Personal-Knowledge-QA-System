package com.aiplus.rag.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String sessionId) {
        super("会话不存在: " + sessionId);
    }
}
