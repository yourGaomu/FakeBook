package com.zhangzc.fakebookspringbootstartjackon.Config;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import com.zhangzc.fakebookspringbootstartjackon.Const.DateConstants;
import com.zhangzc.fakebookspringbootstartjackon.Utils.CustomLocalDateTimeDeserializer;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.fakebookspringbootstartjackon.Utils.FlexibleListDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // 初始化一个 ObjectMapper 对象，用于自定义 Jackson 的行为
        ObjectMapper objectMapper = new ObjectMapper();

        // 忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 设置凡是为 null 的字段，返参中均不返回
        // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 设置时区
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // JavaTimeModule 用于指定序列化和反序列化规则
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 支持 LocalDateTime（兼容时间戳和字符串格式）
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateConstants.DATE_FORMAT_Y_M_D_H_M_S));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new CustomLocalDateTimeDeserializer(DateConstants.DATE_FORMAT_Y_M_D_H_M_S));

        // 支持 LocalDate
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateConstants.DATE_FORMAT_Y_M_D));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateConstants.DATE_FORMAT_Y_M_D));

        // 支持 LocalTime
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateConstants.DATE_FORMAT_H_M_S));
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateConstants.DATE_FORMAT_H_M_S));

        // 支持 YearMonth
        javaTimeModule.addSerializer(YearMonth.class,
                new YearMonthSerializer(DateConstants.DATE_FORMAT_Y_M));
        javaTimeModule.addDeserializer(YearMonth.class,
                new YearMonthDeserializer(DateConstants.DATE_FORMAT_Y_M));

        // 全局 List 反序列化（上下文感知）：
        // - List<String>: 接受字符串/字符串数组
        // - List<Long>: 接受数字/数字数组/字符串数字
        javaTimeModule.addDeserializer(List.class, new FlexibleListDeserializer());

        // 注册模块
        objectMapper.registerModule(javaTimeModule);


        SimpleModule keyConvertModule = new SimpleModule();
        keyConvertModule.addKeyDeserializer(Long.class, new KeyDeserializer() {
            @Override
            public Long deserializeKey(String key, DeserializationContext ctxt) throws IOException {
                try {
                    return key == null ? null : Long.parseLong(key);
                } catch (NumberFormatException e) {
                    throw new IOException("字符串转 Long 失败: " + key, e);
                }
            }
        });
        objectMapper.registerModule(keyConvertModule);


        return objectMapper;
    }

    @Bean
    public JsonUtils jsonUtils() {
        JsonUtils jsonUtils = new JsonUtils();
        JsonUtils.setObjectMapper(objectMapper());
        return jsonUtils;
    }
}

