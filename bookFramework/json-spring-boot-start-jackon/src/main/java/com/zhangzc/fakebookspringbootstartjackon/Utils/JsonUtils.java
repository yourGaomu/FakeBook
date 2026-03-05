package com.zhangzc.fakebookspringbootstartjackon.Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public class JsonUtils {

    // 由自动配置类注入，确保非空
    public static ObjectMapper objectMapper;

    // 禁止外部实例化
    public JsonUtils() {
    }


    /**
     * 由自动配置类调用，注入 ObjectMapper（包内可见，避免外部修改）
     */
     public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    // 重载方法，处理泛型类型
    @SneakyThrows
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        checkInit();
        return objectMapper.readValue(json, typeReference);
    }


    /**
     * 对象序列化为 JSON 字符串
     */
    @SneakyThrows
    public static String toJsonString(Object obj) {
        checkInit();
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * JSON 字符串反序列化为对象
     */
    @SneakyThrows
    public static <T> T parseObject(String json, Class<T> clazz) {
        checkInit();
        return objectMapper.readValue(json, clazz);
    }

    // 检查是否已初始化（避免 NPE）
    private static void checkInit() {
        if (objectMapper == null) {
            throw new IllegalStateException("JsonUtils 未初始化，请检查是否引入了正确的 Starter");
        }
    }

    @SneakyThrows
    public static <T> T parseList(String json, TypeReference<T> typeReference) {
        checkInit();
        return objectMapper.readValue(json, typeReference);
    }
}

