package com.zhangzc.milvusspringbootstart.config.property;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "zhang.milvus")
@Data
@Component
@Primary
public class MilvusProperty {
    private Boolean enable;
    private String uri;
    private String dbName;
    private String username;
    private String password;
    private String token;
    private List<String> packages;
    private Boolean openLog;
    private String logLevel;
    private Boolean banner;


}
