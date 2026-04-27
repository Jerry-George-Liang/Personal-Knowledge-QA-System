package com.aiplus.rag.controller;

import com.aiplus.rag.model.ApiResponse;
import com.aiplus.rag.model.SessionInfo;
import com.aiplus.rag.service.SessionManager;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionManager sessionManager;

    @PostMapping("/create")
    public ApiResponse<Map<String, String>> createSession() {
        String sessionId = sessionManager.createSession();
        log.info("创建新会话: {}", sessionId);
        return ApiResponse.ok(Map.of("sessionId", sessionId, "createdAt",
                String.valueOf(System.currentTimeMillis())));
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable String sessionId) {
        sessionManager.deleteSession(sessionId);
        return ApiResponse.ok("会话已删除", null);
    }

    @GetMapping("/list")
    public ApiResponse<List<SessionInfo>> listSessions() {
        return ApiResponse.ok(sessionManager.listSessions());
    }

    @GetMapping("/{sessionId}/history")
    public ApiResponse<List<Map<String, String>>> getSessionHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = sessionManager.getHistory(sessionId);
        List<Map<String, String>> result = new ArrayList<>();
        for (ChatMessage message : history) {
            Map<String, String> msg = Map.of(
                    "role", message instanceof UserMessage ? "user" : "assistant",
                    "content", message instanceof UserMessage um ? um.singleText() :
                            ((AiMessage) message).text()
            );
            result.add(msg);
        }
        log.info("获取会话 {} 历史消息，共 {} 条", sessionId, result.size());
        return ApiResponse.ok(result);
    }
}
