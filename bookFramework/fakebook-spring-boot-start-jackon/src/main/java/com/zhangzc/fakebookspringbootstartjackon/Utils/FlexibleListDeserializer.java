package com.zhangzc.fakebookspringbootstartjackon.Utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlexibleListDeserializer extends JsonDeserializer<List<?>> implements ContextualDeserializer {

    private final JavaType contextualType;
    private JsonDeserializer<?> elementDeserializer; // 元素类型的原生反序列化器

    public FlexibleListDeserializer() {
        this.contextualType = null;
        this.elementDeserializer = null;
    }

    private FlexibleListDeserializer(JavaType contextualType, JsonDeserializer<?> elementDeserializer) {
        this.contextualType = contextualType;
        this.elementDeserializer = elementDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        // 获取列表的实际类型（如 List<FindNoteCountsByIdRspDTO>）
        JavaType type = property != null ? property.getType() : ctxt.getContextualType();
        // 获取列表元素的类型（如 FindNoteCountsByIdRspDTO）
        JavaType elementType = type.containedType(0);
        // 获取元素类型的原生反序列化器（关键：用于处理复杂对象）
        JsonDeserializer<?> elementDeser = ctxt.findContextualValueDeserializer(elementType, property);

        return new FlexibleListDeserializer(type, elementDeser);
    }

    @Override
    public List<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Class<?> elementClass = resolveElementClass();

        // 处理null值
        if (JsonToken.VALUE_NULL.equals(p.getCurrentToken())) {
            return Collections.emptyList();
        }

        // 处理单值（非数组）情况：包装成单元素列表
        if (isSingleValueToken(p.getCurrentToken())) {
            Object value = deserializeSingleElement(p, ctxt, elementClass);
            return Collections.singletonList(value);
        }

        // 处理数组情况
        if (JsonToken.START_ARRAY.equals(p.getCurrentToken())) {
            List<Object> result = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                // 跳过null元素
                if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
                    continue;
                }
                // 反序列化单个元素
                Object value = deserializeSingleElement(p, ctxt, elementClass);
                result.add(value);
            }
            return result;
        }

        // 其他情况：返回空列表或抛异常
        return Collections.emptyList();
    }

    // 判断是否为单值类型（字符串、数字等）
    private boolean isSingleValueToken(JsonToken token) {
        return token == JsonToken.VALUE_STRING
                || token == JsonToken.VALUE_NUMBER_INT
                || token == JsonToken.VALUE_NUMBER_FLOAT
                || token == JsonToken.VALUE_TRUE
                || token == JsonToken.VALUE_FALSE
                || token == JsonToken.START_OBJECT; // 支持单个对象作为列表元素
    }

    // 反序列化单个元素（根据类型适配）
    private Object deserializeSingleElement(JsonParser p, DeserializationContext ctxt, Class<?> elementClass) throws IOException {
        // 1. 处理复杂对象（使用原生反序列化器）
        if (elementDeserializer != null && !(String.class.equals(elementClass) || Long.class.equals(elementClass))) {
            return elementDeserializer.deserialize(p, ctxt);
        }

        // 2. 处理Long类型
        if (Long.class.equals(elementClass)) {
            if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
                String raw = p.getValueAsString();
                return raw == null || raw.isEmpty() ? null : Long.parseLong(raw);
            } else if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
                return p.getLongValue();
            }
        }

        // 3. 默认处理为String类型
        if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
            return String.valueOf(p.getLongValue());
        }

        // 4. 其他类型直接转为字符串
        return p.getValueAsString();
    }

    private Class<?> resolveElementClass() {
        if (contextualType != null && contextualType.containedTypeCount() > 0) {
            JavaType elem = contextualType.containedType(0);
            if (elem != null) {
                return elem.getRawClass();
            }
        }
        return Object.class;
    }
}
