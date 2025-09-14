package com.zhangzc.fakebookspringbootstartjackon.Utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自定义反序列化器：将单个字符串转为单元素列表，若为数组则正常解析
 */
public class StringToListDeserializer extends JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 尝试获取字符串值
        String value = p.getValueAsString();
        if (value != null) {
            // 若为单个字符串，返回包含该字符串的列表
            return Collections.singletonList(value);
        }
        
        // 若不是字符串，尝试按数组解析（兼容正常的数组格式）
        CollectionType listType = ctxt.getTypeFactory().constructCollectionType(List.class, String.class);
        return ctxt.readValue(p, listType);
    }
}