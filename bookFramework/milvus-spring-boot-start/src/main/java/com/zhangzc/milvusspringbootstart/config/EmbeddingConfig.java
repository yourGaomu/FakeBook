package com.zhangzc.milvusspringbootstart.config;

import com.zhangzc.milvusspringbootstart.config.property.EmbeddingProperty;
import com.zhangzc.milvusspringbootstart.core.service.coreService.RecursiveSliceService;
import com.zhangzc.milvusspringbootstart.core.service.coreService.SentenceSliceService;
import com.zhangzc.milvusspringbootstart.core.service.coreService.SizeSliceService;
import com.zhangzc.milvusspringbootstart.core.service.coreService.SliceCoreService;
import com.zhangzc.milvusspringbootstart.core.service.SliceService;
import com.zhangzc.milvusspringbootstart.utills.EmbeddingUtil;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmbeddingProperty.class)
@Slf4j
public class EmbeddingConfig {

    @ConditionalOnProperty(prefix = "zhang.milvus", name = "enable", havingValue = "true")
    @Bean
    public EmbeddingModel embeddingModel(EmbeddingProperty embeddingProperty) {

        return OpenAiEmbeddingModel.builder()
                .apiKey(embeddingProperty.getApiKey())
                .modelName(embeddingProperty.getModel())
                .baseUrl(embeddingProperty.getBaseUrl())
                .dimensions(embeddingProperty.getDimensions())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(SliceCoreService.class)
    public SliceCoreService sliceCoreService(EmbeddingProperty embeddingProperty) {
        log.info("用户采用自带的嵌入模式");
        if (embeddingProperty.getSliceMode().equals(EmbeddingProperty.SliceDefaultMode.SIZE.getMode())) {
            log.info("用户使用的分块模式是根据字数来分块");
            return new SizeSliceService(embeddingProperty);
        } else if (embeddingProperty.getSliceMode().equals(EmbeddingProperty.SliceDefaultMode.SENTENCE.getMode())) {
            log.info("用户采用的是根据句子来分块");
            return new SentenceSliceService();
        } else if (embeddingProperty.getSliceMode().equals(EmbeddingProperty.SliceDefaultMode.RECURSIVE.getMode())) {
            log.info("用户采用的是递归分块模式(LangChain4j)");
            return new RecursiveSliceService(embeddingProperty);
        } else {
            throw new ClassCastException("用户没有提供自定义的分片方法或者系统自带的分片方法有误");
        }
    }


    @Bean
    @ConditionalOnProperty(prefix = "zhang.milvus", name = "enable", havingValue = "true")
    @ConditionalOnMissingBean(EmbeddingUtil.class)
    public EmbeddingUtil embeddingUtil(EmbeddingModel embeddingModel, SliceService sliceService) {
        return new EmbeddingUtil(embeddingModel, sliceService);
    }
}
