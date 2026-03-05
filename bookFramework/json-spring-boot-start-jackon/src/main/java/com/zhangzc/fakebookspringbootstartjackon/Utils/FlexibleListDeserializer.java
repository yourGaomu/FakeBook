package com.zhangzc.fakebookspringbootstartjackon.Utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlexibleListDeserializer extends JsonDeserializer<List<?>> implements ContextualDeserializer {

    private final JavaType contextualType;
    private JsonDeserializer<?> elementDeserializer;

    public FlexibleListDeserializer() {
        this.contextualType = TypeFactory.defaultInstance().constructCollectionType(List.class, Object.class);
        this.elementDeserializer = null;
    }

    private FlexibleListDeserializer(JavaType contextualType, JsonDeserializer<?> elementDeserializer) {
        this.contextualType = contextualType != null ? contextualType :
                TypeFactory.defaultInstance().constructCollectionType(List.class, Object.class);
        this.elementDeserializer = elementDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType type = null;
        if (property != null) {
            type = property.getType();
        }
        if (type == null && ctxt != null) {
            type = ctxt.getContextualType();
        }
        if (type == null) {
            type = TypeFactory.defaultInstance().constructCollectionType(List.class, Object.class);
        }

        if (!type.isCollectionLikeType()) {
            // 通过 ctxt 获取当前 JsonParser，构造异常
            JsonParser p = ctxt.getParser();
            throw new JsonMappingException(p, "FlexibleListDeserializer only supports Collection/List type, but got: " + type);
        }

        JavaType elementType = type.containedTypeCount() > 0 ? type.containedType(0) :
                TypeFactory.defaultInstance().constructType(Object.class);

        JsonDeserializer<?> elementDeser = ctxt.findContextualValueDeserializer(elementType, property);

        return new FlexibleListDeserializer(type, elementDeser);
    }

    @Override
    public List<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Class<?> elementClass = resolveElementClass();

        if (JsonToken.VALUE_NULL.equals(p.getCurrentToken())) {
            return Collections.emptyList();
        }

        if (isSingleValueToken(p.getCurrentToken())) {
            Object value = deserializeSingleElement(p, ctxt, elementClass);
            return value == null ? Collections.emptyList() : Collections.singletonList(value);
        }

        if (JsonToken.START_ARRAY.equals(p.getCurrentToken())) {
            List<Object> result = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
                    continue;
                }
                Object value = deserializeSingleElement(p, ctxt, elementClass);
                if (value != null) {
                    result.add(value);
                }
            }
            return result;
        }

        ctxt.reportBadDefinition(contextualType, "Unsupported JSON token for List deserialization: " + p.getCurrentToken());
        return Collections.emptyList();
    }

    private boolean isSingleValueToken(JsonToken token) {
        return token == JsonToken.VALUE_STRING
                || token == JsonToken.VALUE_NUMBER_INT
                || token == JsonToken.VALUE_NUMBER_FLOAT
                || token == JsonToken.VALUE_TRUE
                || token == JsonToken.VALUE_FALSE
                || token == JsonToken.START_OBJECT;
    }

    private Object deserializeSingleElement(JsonParser p, DeserializationContext ctxt, Class<?> elementClass) throws IOException {
        if (elementDeserializer != null && !(String.class.equals(elementClass) || Long.class.equals(elementClass))) {
            try {
                return elementDeserializer.deserialize(p, ctxt);
            } catch (Exception e) {
                ctxt.reportBadDefinition(ctxt.constructType(elementClass), "Failed to deserialize element: " + e.getMessage());
                return null;
            }
        }

        if (Long.class.equals(elementClass) || long.class.equals(elementClass)) {
            if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
                String raw = p.getValueAsString().trim();
                return raw.isEmpty() ? null : Long.parseLong(raw);
            } else if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
                return p.getLongValue();
            } else {
                ctxt.reportBadDefinition(ctxt.constructType(Long.class), "Unsupported type for Long: " + p.getCurrentToken());
                return null;
            }
        }

        if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT || p.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
            return String.valueOf(p.getNumberValue());
        } else if (p.getCurrentToken() == JsonToken.VALUE_TRUE || p.getCurrentToken() == JsonToken.VALUE_FALSE) {
            return String.valueOf(p.getBooleanValue());
        } else if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(mapper.readTree(p));
        }

        return p.getValueAsString();
    }

    private Class<?> resolveElementClass() {
        if (contextualType == null) {
            return Object.class;
        }
        JavaType elemType = contextualType.containedTypeCount() > 0 ? contextualType.containedType(0) : null;
        return elemType != null ? elemType.getRawClass() : Object.class;
    }
}