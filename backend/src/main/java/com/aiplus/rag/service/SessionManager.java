package com.aiplus.rag.service;

import com.aiplus.rag.exception.SessionNotFoundException;
import com.aiplus.rag.model.SessionInfo;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.ChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class SessionManager {

    private static final int MAX_MESSAGES_PER_SESSION = 20;

    private final ConcurrentHashMap<String, ChatMemory> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> creationTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> messageCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionTitles = new ConcurrentHashMap<>();

    public String createSession() {
        String sessionId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        ChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(MAX_MESSAGES_PER_SESSION)
                .build();
        sessions.put(sessionId, memory);
        creationTimes.put(sessionId, System.currentTimeMillis());
        messageCounts.put(sessionId, new AtomicInteger(0));
        log.info("创建新会话: {}", sessionId);
        return sessionId;
    }

    public ChatMemory getSession(String sessionId) {
        ChatMemory memory = sessions.get(sessionId);
        if (memory == null) {
            throw new SessionNotFoundException(sessionId);
        }
        return memory;
    }

    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
        creationTimes.remove(sessionId);
        messageCounts.remove(sessionId);
        sessionTitles.remove(sessionId);
        log.info("删除会话: {}", sessionId);
    }

    public List<SessionInfo> listSessions() {
        List<SessionInfo> result = new ArrayList<>();
        sessions.forEach((id, memory) -> {
            int count = messageCounts.getOrDefault(id, new AtomicInteger(0)).get();
            result.add(SessionInfo.builder()
                    .sessionId(id)
                    .createdAt(creationTimes.getOrDefault(id, 0L))
                    .messageCount(count)
                    .title(sessionTitles.getOrDefault(id, "新对话"))
                    .build());
        });
        return result;
    }

    public void addMessage(String sessionId, ChatMessage message) {
        if (sessionId == null || sessionId.isBlank()) return;
        ChatMemory memory = sessions.get(sessionId);
        if (memory != null) {
            if (message instanceof UserMessage um) {
                memory.add(um);
                incrementCount(sessionId);
                sessionTitles.putIfAbsent(sessionId, um.singleText());
            } else if (message instanceof AiMessage am) {
                memory.add(am);
                incrementCount(sessionId);
            }
        }
    }

    public void updateSessionTitle(String sessionId, String title) {
        if (sessionId != null && !sessionId.isBlank() && title != null && !title.isBlank()) {
            sessionTitles.putIfAbsent(sessionId, title.length() > 50 ? title.substring(0, 47) + "..." : title);
        }
    }

    private void incrementCount(String sessionId) {
        messageCounts.computeIfAbsent(sessionId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public List<ChatMessage> getHistory(String sessionId) {
        ChatMemory memory = sessions.get(sessionId);
        if (memory == null) return List.of();
        try {
            return new ArrayList<>(memory.messages());
        } catch (Exception e) {
            log.warn("获取会话 {} 历史消息失败: {}", sessionId, e.getMessage());
            return List.of();
        }
    }

    public void clearAllSessions() {
        int count = sessions.size();
        sessions.clear();
        creationTimes.clear();
        messageCounts.clear();
        sessionTitles.clear();
        log.info("已清除所有会话，共 {} 个", count);
    }

    public boolean exists(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}
