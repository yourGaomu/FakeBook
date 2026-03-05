package com.zhangzc.fakebookspringbootstartjackon.Utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 兼容时间戳和字符串的 LocalDateTime 反序列化器
 */
public class CustomLocalDateTimeDeserializer extends LocalDateTimeDeserializer {

    // 传入自定义的日期格式（如 yyyy-MM-dd HH:mm:ss）
    public CustomLocalDateTimeDeserializer(DateTimeFormatter formatter) {
        super(formatter);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 尝试先按数字时间戳解析（毫秒级）
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            long timestamp = p.getLongValue();
            // 时间戳转 LocalDateTime（使用上海时区）
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.of("Asia/Shanghai")
            );
        }
        // 若不是数字，再按字符串格式解析（复用父类逻辑）
        return super.deserialize(p, ctxt);
    }
}