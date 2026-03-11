package com.zhangzc.milvusspringbootstart.config.property;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zhang.rerank")
@Data
public class ReRankProperty {
    // Rerank 配置
    private Boolean enableRerank = false;
    private String rerankModel = "gte-rerank"; // DashScope 默认 Rerank 模型
    private String rerankApiKey;

}
