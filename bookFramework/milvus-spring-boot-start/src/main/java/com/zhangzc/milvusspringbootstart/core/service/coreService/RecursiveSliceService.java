package com.zhangzc.milvusspringbootstart.core.service.coreService;

import com.zhangzc.milvusspringbootstart.config.property.EmbeddingProperty;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RecursiveSliceService implements SliceCoreService {

    private final EmbeddingProperty embeddingProperty;

    @Override
    public List<String> slice(String text, int size) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        
        int overlap = embeddingProperty.getSliceOverlap() != null ? embeddingProperty.getSliceOverlap() : 0;
        
        // Safety check: overlap must be strictly less than size
        if (overlap >= size) {
            log.warn("Slice overlap ({}) must be less than size ({}), adjusting overlap to size / 10", overlap, size);
            overlap = Math.max(0, size / 10);
        }

        try {
            DocumentSplitter splitter = DocumentSplitters.recursive(size, overlap);
            List<TextSegment> segments = splitter.split(Document.from(text));
            
            return segments.stream()
                    .map(TextSegment::text)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to slice text recursively", e);
            // Fallback or rethrow? Let's rethrow or return empty to avoid breaking the flow silently but log error
            throw new RuntimeException("Recursive slicing failed", e);
        }
    }
}
