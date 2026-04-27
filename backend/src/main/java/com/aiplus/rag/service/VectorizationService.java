package com.aiplus.rag.service;

import com.aiplus.rag.model.DocumentMetadata;
import com.aiplus.rag.model.DocumentParseResult;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorizationService {

    private final EmbeddingModel embeddingModel;
    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private final DocumentSplitService documentSplitService;
    private final ConcurrentHashMap<String, DocumentMetadata> documentRegistry;
    private final ConcurrentHashMap<String, List<String>> documentEmbeddingIds = new ConcurrentHashMap<>();

    public DocumentMetadata vectorize(DocumentParseResult parseResult) {
        String fileName = parseResult.getFileName();
        log.info("开始向量化文件: {}，原始段落数: {}", fileName, parseResult.getSegmentCount());

        List<TextSegment> segments = documentSplitService.split(
                parseResult.getDocuments(), fileName);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        List<String> embeddingIds = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            String id = embeddingStore.add(embeddings.get(i), segments.get(i));
            embeddingIds.add(id);
        }

        documentEmbeddingIds.put(parseResult.getDocumentId(), embeddingIds);

        DocumentMetadata metadata = DocumentMetadata.builder()
                .documentId(parseResult.getDocumentId())
                .fileName(fileName)
                .uploadTime(LocalDateTime.now())
                .segmentCount(segments.size())
                .build();

        documentRegistry.put(parseResult.getDocumentId(), metadata);

        log.info("向量化完成: {}，最终段落数: {}，向量维度: {}",
                fileName, segments.size(),
                embeddings.isEmpty() ? 0 : embeddings.get(0).dimension());

        return metadata;
    }

    public List<DocumentMetadata> listDocuments() {
        return List.copyOf(documentRegistry.values());
    }

    public void clearAll() {
        embeddingStore.removeAll();
        documentRegistry.clear();
        documentEmbeddingIds.clear();
        log.info("已清除所有向量数据和文档元数据");
    }

    public boolean deleteDocument(String documentId) {
        DocumentMetadata metadata = documentRegistry.get(documentId);
        if (metadata == null) {
            log.warn("文档不存在: {}", documentId);
            return false;
        }

        List<String> idsToRemove = documentEmbeddingIds.get(documentId);
        if (idsToRemove != null && !idsToRemove.isEmpty()) {
            embeddingStore.removeAll(idsToRemove);
            documentEmbeddingIds.remove(documentId);
        }

        documentRegistry.remove(documentId);
        log.info("已删除文档: {}，移除 {} 个段落", metadata.getFileName(),
                idsToRemove != null ? idsToRemove.size() : 0);
        return true;
    }

    public int getTotalSegments() {
        return documentRegistry.values().stream()
                .mapToInt(DocumentMetadata::getSegmentCount)
                .sum();
    }
}
