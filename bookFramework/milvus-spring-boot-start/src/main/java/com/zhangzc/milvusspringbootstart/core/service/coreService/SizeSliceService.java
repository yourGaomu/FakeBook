package com.zhangzc.milvusspringbootstart.core.service.coreService;

import com.zhangzc.milvusspringbootstart.config.property.EmbeddingProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class SizeSliceService implements SliceCoreService {
    private final EmbeddingProperty embeddingProperty;

    /**
     * 按照文字的字数来分块
     * */
    /**
     * 按固定字符长度切片（保留完整语义，避免切断词语）
     * @param text 原始文本
     * @param sliceLength 每个切片的最大长度
     * @return 切片后的文本列表
     */
    public List<String> slice(String text, int sliceLength) {
        if (sliceLength <=0){
            sliceLength = embeddingProperty.getDefaultSliceSize();
        }

        List<String> slices = new ArrayList<>();
        if (StringUtils.isBlank(text) || sliceLength <= 0) {
            return slices;
        }

        // 清洗文本：保留中文、英文、数字，去除特殊符号
        String cleanText = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", "")
                .trim();

        int start = 0;
        int end = sliceLength;
        int textLength = cleanText.length();

        while (start < textLength) {
            // 避免越界
            end = Math.min(end, textLength);

            // 优化：如果不是最后一个切片，且当前结束位置不是空格/标点，往前找最近的空格（避免切断词语）
            if (end < textLength && !Character.isWhitespace(cleanText.charAt(end))) {
                int lastSpace = cleanText.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            String slice = cleanText.substring(start, end).trim();
            if (!slice.isEmpty()) {
                slices.add(slice);
            }

            // 移动到下一个切片
            start = end;
            end = start + sliceLength;
        }

        return slices;
    }
}
