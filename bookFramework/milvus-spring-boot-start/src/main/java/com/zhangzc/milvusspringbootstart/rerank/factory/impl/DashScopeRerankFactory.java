package com.zhangzc.milvusspringbootstart.rerank.factory.impl;

import com.zhangzc.milvusspringbootstart.rerank.CustomDashScopeScoringModel;
import com.zhangzc.milvusspringbootstart.rerank.domain.TLlmModel;
import com.zhangzc.milvusspringbootstart.rerank.factory.RerankModelFactory;
import dev.langchain4j.model.scoring.ScoringModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Factory for creating DashScope (Aliyun) Rerank models
 */
@Component
@Slf4j
public class DashScopeRerankFactory implements RerankModelFactory {

    @Override
    public String getProvider() {
        return "dashscope";
    }

    @Override
    public ScoringModel createModel(TLlmModel config) {
        log.info("Creating DashScope Rerank Model: {}", config.getModelCode());
        return new CustomDashScopeScoringModel(
                config.getApiKey(),
                config.getModelCode()
        );
    }
}
