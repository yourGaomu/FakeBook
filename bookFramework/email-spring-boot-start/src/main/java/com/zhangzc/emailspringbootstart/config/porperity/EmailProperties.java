package com.zhangzc.emailspringbootstart.config.porperity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zhang.email")
@Setter
@Getter
public class EmailProperties {

    /**
     * Whether to enable the Kafka consumer for sending emails.
     */
    private boolean enableConsumer = false;

}
