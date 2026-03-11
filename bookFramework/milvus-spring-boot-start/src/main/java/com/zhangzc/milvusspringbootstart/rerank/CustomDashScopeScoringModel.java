package com.zhangzc.milvusspringbootstart.rerank;



import com.alibaba.dashscope.utils.Constants;
import com.alibaba.dashscope.rerank.TextReRank;
import com.alibaba.dashscope.rerank.TextReRankParam;
import com.alibaba.dashscope.rerank.TextReRankResult;
import com.alibaba.dashscope.rerank.TextReRankOutput;
import com.zhangzc.milvusspringbootstart.annotation.AiMonitor;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义实现 DashScope ScoringModel (基于官方 SDK)
 * 避免 langchain4j-community 依赖问题
 */
@Slf4j
public class CustomDashScopeScoringModel implements ScoringModel {

    private final String apiKey;
    private final String modelName;
    private final TextReRank textReRank;

    public CustomDashScopeScoringModel(String apiKey, String modelName) {
        this.apiKey = apiKey;
        this.modelName = modelName;
        this.textReRank = new TextReRank();
        // 设置全局 API Key (如果 SDK 需要)
        if (apiKey != null && !apiKey.isEmpty()) {
             Constants.apiKey = apiKey;
        }
    }

    @Override
    @AiMonitor("DashScope Rerank")
    public Response<List<Double>> scoreAll(List<TextSegment> segments, String query) {
        if (segments == null || segments.isEmpty()) {
            return Response.from(Collections.emptyList());
        }

        List<String> documents = segments.stream()
                .map(TextSegment::text)
                .collect(Collectors.toList());

        try {
            TextReRankParam param = TextReRankParam.builder()
                    .apiKey(apiKey)
                    .model(modelName)
                    .query(query)
                    .documents(documents)
                    .topN(documents.size()) // 返回所有结果的分数
                    .returnDocuments(false) // 只需要分数
                    .build();

            TextReRankResult result = textReRank.call(param);
            
            // 初始化结果列表，长度与输入一致，初始值为 null 或 0.0
            List<Double> orderedScores = new ArrayList<>(Collections.nCopies(documents.size(), 0.0));

            // DashScope 返回的是 List<TextReRankOutput.Result>，包含 index 和 relevance_score
            List<TextReRankOutput.Result> scores = result.getOutput().getResults();
            
            for (TextReRankOutput.Result scoreItem : scores) {
                if (scoreItem.getIndex() != null && scoreItem.getIndex() < orderedScores.size()) {
                    orderedScores.set(scoreItem.getIndex(), scoreItem.getRelevanceScore());
                }
            }

            return Response.from(orderedScores);

        } catch (Exception e) {
            log.error("DashScope Rerank failed", e);
            throw new RuntimeException("DashScope Rerank failed", e);
        }
    }
}
