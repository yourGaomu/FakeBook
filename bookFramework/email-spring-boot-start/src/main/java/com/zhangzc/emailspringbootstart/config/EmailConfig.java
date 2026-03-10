package com.zhangzc.emailspringbootstart.config;

import com.zhangzc.emailspringbootstart.config.porperity.EmailProperties;
import com.zhangzc.emailspringbootstart.consume.Kafka4SmsConsume;
import com.zhangzc.emailspringbootstart.corn.MailHelper;
import com.zhangzc.emailspringbootstart.utils.EmailUtil;
import com.zhangzc.kafkaspringbootstart.utills.KafkaUtills;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfig {

    @Bean
    @ConditionalOnMissingBean
    public EmailUtil emailUtil(KafkaUtills kafkaUtills) {
        return new EmailUtil(kafkaUtills);
    }

    @Bean
    @ConditionalOnMissingBean
    public MailHelper mailHelper(JavaMailSender javaMailSender, MailProperties mailProperties, @Qualifier("emailTaskExecutor") Executor emailTaskExecutor) {
        return new MailHelper(javaMailSender, mailProperties, emailTaskExecutor);
    }

    @Bean
    @ConditionalOnProperty(prefix = "zhang.email", name = "enable-consumer", havingValue = "true")
    public Kafka4SmsConsume kafka4SmsConsume(MailHelper mailHelper) {
        return new Kafka4SmsConsume(mailHelper);
    }

    /**
     * 提供一个邮件专用的线程池
     */
    @Bean("emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("email-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
