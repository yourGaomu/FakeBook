package com.zhangzc.milvusspringbootstart.utills;

import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.milvusspringbootstart.annotation.AiMonitor;
import com.zhangzc.milvusspringbootstart.core.service.SliceService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class EmbeddingUtil {
    private final EmbeddingModel embeddingModel;
    private final SliceService sliceService;

    @AiMonitor("Embedding Single Object")
    public List<Float> embed(Object object) {
        return embeddingModel.embed(JsonUtils.toJsonString(object)).content().vectorAsList();
    }

    /**
    * 只有当选择了嵌入mode是根据自定义大小，第二个函数才会生效
    * 注意：此方法将多个片段的向量拼接成一个超长向量，这通常只适用于特殊模型或需求。
    * 如果你的 Milvus 集合定义的维度是固定的（例如 1536），拼接会导致维度不匹配错误！
    * 通常 RAG 场景下，我们会将每个片段存储为独立的 Milvus 行，而不是拼接向量。
    * */
    @AiMonitor("Embedding with Slicing")
    public Map<String, List<Float>> embed(String text, int size) {
        // 使用 LinkedHashMap 保持顺序
        Map<String, List<Float>> resultMap = new java.util.LinkedHashMap<>();
        try {
            //进行切片操作
            List<String> slice = sliceService.slice(text, size);
            List<TextSegment> list = slice.stream().map(TextSegment::from).toList();
            Response<List<Embedding>> listResponse = embeddingModel.embedAll(list);

            // 将所有片段的向量依次添加到结果列表中
            // 假设 listResponse.content() 的顺序与 slice 列表的顺序一致
            List<Embedding> embeddings = listResponse.content();
            if (embeddings.size() != slice.size()) {
                log.warn("Embedding count {} does not match slice count {}", embeddings.size(), slice.size());
            }

            for (int i = 0; i < Math.min(embeddings.size(), slice.size()); i++) {
                String segmentText = slice.get(i);
                List<Float> vector = embeddings.get(i).vectorAsList();
                // 使用片段文本作为 Key (注意：如果片段文本重复，可能会覆盖，但在 RAG 切片中通常相邻片段不同)
                // 为了保险起见，或者如果需要绝对唯一，可以考虑返回 List<Pair<String, List<Float>>>
                // 但根据用户要求返回 Map<String, List<Float>>
                resultMap.put(segmentText, vector);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
        return resultMap;
    }

    /**
    * 普通的文字列表嵌入
    * */
    @AiMonitor("Embedding Text List")
    public List<List<Float>> embed(List<String> texts) {
        List<List<Float>> result = new ArrayList<>();
        texts.forEach(text -> {
            List<Float> floats = embeddingModel.embed(text).content().vectorAsList();
            result.add(floats);
        });
        return result;
    }
}
