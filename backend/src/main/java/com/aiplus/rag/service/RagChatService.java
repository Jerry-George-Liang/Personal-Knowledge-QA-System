package com.aiplus.rag.service;

import com.aiplus.rag.config.RetrieverConfig.RagRetriever;
import com.aiplus.rag.model.Citation;
import com.aiplus.rag.model.ChatResponse;
import com.aiplus.rag.service.prompt.RagPromptTemplate;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final RagRetriever ragRetriever;
    private final SessionManager sessionManager;

    public ChatResponse chat(String question, String sessionId) {
        log.info("收到问答请求: sessionId={}, question={}", sessionId, truncate(question, 50));

        List<TextSegment> relevantSegments = ragRetriever.retrieve(question);

        boolean hasRelevantContext = !relevantSegments.isEmpty();

        if (!hasRelevantContext) {
            log.warn("未检索到相关文档内容, question={}", truncate(question, 50));
        }

        String context = buildContext(relevantSegments);
        List<Citation> citations = buildCitations(relevantSegments);

        String fullUserMessage = buildFullPrompt(question, context);

        dev.langchain4j.model.chat.response.ChatResponse llmResponse = chatLanguageModel.chat(
                ChatRequest.builder().messages(UserMessage.from(fullUserMessage)).build());
        String answer = llmResponse.aiMessage().text();

        sessionManager.addMessage(sessionId, UserMessage.from(question));
        sessionManager.addMessage(sessionId, AiMessage.from(answer));

        return ChatResponse.builder()
                .answer(answer)
                .citations(citations)
                .sessionId(sessionId)
                .hasRelevantContext(hasRelevantContext)
                .build();
    }

    private String buildFullPrompt(String question, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append(RagPromptTemplate.SYSTEM_PROMPT).append("\n\n");
        if (!context.isBlank()) {
            sb.append(context).append("\n\n");
        }
        sb.append(RagPromptTemplate.buildUserMessage(question, context));
        return sb.toString();
    }

    private String buildContext(List<TextSegment> segments) {
        if (segments.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            String sourceFile = segment.metadata().getString("source_file");
            sb.append("[片段%d] 来源: %s\n%s\n\n".formatted(i + 1,
                    sourceFile != null ? sourceFile : "未知", segment.text()));
        }
        return sb.toString();
    }

    private List<Citation> buildCitations(List<TextSegment> segments) {
        List<Citation> citations = new ArrayList<>();
        for (TextSegment segment : segments) {
            String sourceFile = segment.metadata().getString("source_file");
            citations.add(Citation.builder()
                    .sourceFileName(sourceFile != null ? sourceFile : "未知")
                    .content(segment.text())
                    .relevanceScore(0.8)
                    .build());
        }
        return citations;
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return "";
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}
