package com.zhangzc.milvusspringbootstart.rerank.service;


import com.zhangzc.milvusspringbootstart.rerank.domain.TLlmModel;
import com.zhangzc.milvusspringbootstart.rerank.factory.RerankModelFactory;
import dev.langchain4j.model.scoring.ScoringModel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service to manage and retrieve Rerank models dynamically
 */
@Service
@Slf4j
public class RerankModelService {

    private final TLlmModelService tLlmModelService;
    private final Map<String, RerankModelFactory> factoryMap;
    private final Map<Long, ScoringModel> modelCache = new ConcurrentHashMap<>();

    public RerankModelService(TLlmModelService tLlmModelService, List<RerankModelFactory> factories) {
        this.tLlmModelService = tLlmModelService;
        this.factoryMap = factories.stream()
                .collect(Collectors.toMap(RerankModelFactory::getProvider, f -> f));
    }


    /**
     * Get a ScoringModel by ID. If id is null, returns the default enabled model.
     */
    public ScoringModel getScoringModel(Long modelId) {
        TLlmModel modelConfig;
        if (modelId == null) {
            // Find default enabled rerank model
            modelConfig = tLlmModelService.lambdaQuery()
                    .eq(TLlmModel::getIsEnable, 1)
                    .and(w -> w.eq(TLlmModel::getModelType, "rerank")
                            .or()
                            .like(TLlmModel::getModelCode, "rerank"))
                    .last("LIMIT 1")
                    .one();
            
            if (modelConfig == null) {
                // Fallback to any enabled model if specific rerank type not found (though risky)
                log.warn("No specific 'rerank' model found, trying any enabled model as fallback.");
                 modelConfig = tLlmModelService.lambdaQuery()
                        .eq(TLlmModel::getIsEnable, 1)
                        .last("LIMIT 1")
                        .one();
            }

            if (modelConfig == null) {
                throw new RuntimeException("No enabled Rerank model found in database.");
            }
            modelId = modelConfig.getId();
        } 
        
        // Check cache first with resolved ID
        if (modelCache.containsKey(modelId)) {
            return modelCache.get(modelId);
        }

        // Fetch config if not already fetched
        modelConfig = tLlmModelService.getById(modelId);
        
        if (modelConfig == null) {
            throw new RuntimeException("Rerank model configuration not found for id: " + modelId);
        }

        return buildAndCacheModel(modelConfig);
    }

    private synchronized ScoringModel buildAndCacheModel(TLlmModel config) {
        // Double check cache
        if (modelCache.containsKey(config.getId())) {
            return modelCache.get(config.getId());
        }

        String provider = config.getProvider();
        if (provider == null || provider.isEmpty()) {
             provider = "dashscope"; 
        }
        
        // Normalize provider string
        provider = provider.toLowerCase();

        RerankModelFactory factory = factoryMap.get(provider);
        if (factory == null) {
            throw new RuntimeException("No factory found for Rerank provider: " + provider);
        }

        ScoringModel model = factory.createModel(config);
        modelCache.put(config.getId(), model);
        return model;
    }
}
