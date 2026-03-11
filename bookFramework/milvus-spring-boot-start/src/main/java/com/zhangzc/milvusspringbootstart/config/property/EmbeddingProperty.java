package com.zhangzc.milvusspringbootstart.config.property;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zhang.embedding")
@Data
public class EmbeddingProperty {
    private String model;
    private String endpoint;
    private String apiKey;
    private String baseUrl;
    private Integer dimensions;
    private Integer defaultSliceSize = 512;
    private String sliceMode = "sentence";
    private Integer sliceOverlap = 200;

    @Getter
    public enum SliceDefaultMode{
        SENTENCE("sentence"),
        SIZE("size"),
        RECURSIVE("recursive");
        private String mode;
        SliceDefaultMode(String mode) {
            this.mode=mode;
        }
    }
}
