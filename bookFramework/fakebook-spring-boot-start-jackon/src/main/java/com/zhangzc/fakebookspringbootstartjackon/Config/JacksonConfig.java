package com.zhangzc.fakebookspringbootstartjackon.Config;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import com.zhangzc.fakebookspringbootstartjackon.Const.DateConstants;
import com.zhangzc.fakebookspringbootstartjackon.Utils.CustomLocalDateTimeDeserializer;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.fakebookspringbootstartjackon.Utils.StringToListDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        // 支持 List<String> 反序列化（单个字符串转列表）
        javaTimeModule.addDeserializer(List.class, new StringToListDeserializer());

        // 注册模块
        objectMapper.registerModule(javaTimeModule);

        return objectMapper;
    }

    @Bean
    public JsonUtils jsonUtils() {
        JsonUtils jsonUtils = new JsonUtils();
        JsonUtils.setObjectMapper(objectMapper());
        return jsonUtils;
    }
}

