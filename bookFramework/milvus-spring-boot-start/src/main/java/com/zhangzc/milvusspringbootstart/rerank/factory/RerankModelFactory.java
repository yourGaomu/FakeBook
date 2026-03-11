package com.zhangzc.milvusspringbootstart.rerank.factory;

import com.zhangzc.milvusspringbootstart.rerank.domain.TLlmModel;
import dev.langchain4j.model.scoring.ScoringModel;

/**
 * Strategy interface for creating Rerank Scoring Models
 */
public interface RerankModelFactory {
    /**
     * Get the provider name (e.g., "dashscope", "bge")
     */
    String getProvider();

    /**
     * Create a ScoringModel instance based on configuration
     */
    ScoringModel createModel(TLlmModel config);
}
