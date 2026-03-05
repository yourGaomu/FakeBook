package com.zhangzc.fakebookspringbootstartjackon.Utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringToListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken currentToken = p.getCurrentToken();

        // 处理字符串类型：直接转为单元素列表
        if (JsonToken.VALUE_STRING.equals(currentToken)) {
            return Collections.singletonList(p.getValueAsString());
        }

        // 处理数组类型：手动解析每个元素，完全绕开反序列化器查找
        if (JsonToken.START_ARRAY.equals(currentToken)) {
            List<String> resultList = new ArrayList<>();

            // 循环读取数组中的每个元素
            while (p.nextToken() != JsonToken.END_ARRAY) {
                // 只处理字符串元素，其他类型可根据业务需求调整
                if (JsonToken.VALUE_STRING.equals(p.getCurrentToken())) {
                    resultList.add(p.getValueAsString());
                } else {
                    // 遇到非字符串元素时的处理：跳过或抛异常
                    p.skipChildren(); // 跳过该元素
                    // 或抛异常：throw new IOException("数组中只能包含字符串元素");
                }
            }
            return resultList;
        }

        // 处理null值
        if (JsonToken.VALUE_NULL.equals(currentToken)) {
            return Collections.emptyList();
        }

        // 处理其他不支持的类型
        throw new IOException("不支持的JSON类型: " + currentToken + "，仅支持字符串和数组");
    }
}
