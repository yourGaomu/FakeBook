package com.zhangzc.milvusspringbootstart.core.service.coreService;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class SentenceSliceService implements SliceCoreService {
    /**
     * 按句子切片（按。！？分割）
     */
    public List<String> slice(String text, int size) {
        List<String> sentences = new ArrayList<>();
        if (StringUtils.isBlank(text)) {
            return sentences;
        }

        // 按中文句末标点分割
        String[] sentenceArray = text.split("[。！？；]");
        for (String sentence : sentenceArray) {
            sentence = sentence.trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }
        return sentences;
    }

}
