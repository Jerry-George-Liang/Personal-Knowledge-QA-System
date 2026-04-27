package com.aiplus.rag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class RetrieverConfig {

    @Value("${rag.retrieval.top-k:5}")
    private int topK;

    @Value("${rag.retrieval.min-score:0.5}")
    private double minScore;

    @Bean
    public RagRetriever ragRetriever(
            EmbeddingModel embeddingModel,
            InMemoryEmbeddingStore<TextSegment> embeddingStore) {
        return new RagRetriever(embeddingModel, embeddingStore, topK, minScore);
    }

    public static class RagRetriever {

        private final EmbeddingModel embeddingModel;
        private final InMemoryEmbeddingStore<TextSegment> embeddingStore;
        private final int maxResults;
        private final double minScore;

        public RagRetriever(EmbeddingModel embeddingModel,
                           InMemoryEmbeddingStore<TextSegment> embeddingStore,
                           int maxResults, double minScore) {
            this.embeddingModel = embeddingModel;
            this.embeddingStore = embeddingStore;
            this.maxResults = maxResults;
            this.minScore = minScore;
        }

        public List<TextSegment> retrieve(String queryText) {
            Embedding queryEmbedding = embeddingModel.embed(queryText).content();

            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(minScore)
                    .build();

            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

            return result.matches().stream()
                    .map(EmbeddingMatch::embedded)
                    .collect(Collectors.toList());
        }
    }
}
