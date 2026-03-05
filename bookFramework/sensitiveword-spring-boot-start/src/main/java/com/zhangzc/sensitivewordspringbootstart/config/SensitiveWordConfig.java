package com.zhangzc.sensitivewordspringbootstart.config;

import com.zhangzc.sensitivewordspringbootstart.core.MyWordReplace;
import com.zhangzc.sensitivewordspringbootstart.utills.SensitiveWordUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@Slf4j
@ConditionalOnProperty(
        prefix = "zhang.sensitive", // 配置属性的前缀
        name = "enable",            // 配置属性的名称（完整属性：zhang.sensitive.enable）
        havingValue = "true",       // 期望的属性值（必须是true才生效）
        matchIfMissing = false      // 配置缺失时是否匹配：false表示没配置该属性则不生效
)
public class SensitiveWordConfig {
    @PostConstruct
    public void init() {
       log.info("敏感词过滤器初始化中");
    }

    @Bean
    public MyWordReplace myWordReplace() {
        return new MyWordReplace();
    }


    @Bean
    public SensitiveWordUtil sensitiveWordUtil(MyWordReplace myWordReplace) {
        return new SensitiveWordUtil(myWordReplace);
    }


}
