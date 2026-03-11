package com.zhangzc.milvusspringbootstart.config;

import com.zhangzc.milvusspringbootstart.config.property.ReRankProperty;
import com.zhangzc.milvusspringbootstart.rerank.service.RerankModelService;
import dev.langchain4j.model.scoring.ScoringModel;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@EnableConfigurationProperties(ReRankProperty.class)
@MapperScan("com.zhangzc.milvusspringbootstart.rerank.mapper")
@Slf4j
public class RerankConfig {

    /**
     * Creates a default ScoringModel bean.
     * This bean delegates to RerankModelService to fetch the default model from the database.
     * It's marked as @Lazy to avoid early initialization issues with DB connections.
     */
    @Bean
    @ConditionalOnProperty(prefix = "zhang.rerank", name = "enable-rerank", havingValue = "true")
    @Lazy
    public ScoringModel scoringModel(RerankModelService rerankModelService) {
        log.info("Initializing Default Rerank Model via Service");
        // Get the default model (id = null)
        return rerankModelService.getScoringModel(null);
    }
}
