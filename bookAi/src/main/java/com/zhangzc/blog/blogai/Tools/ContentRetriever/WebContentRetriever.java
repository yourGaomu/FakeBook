package com.zhangzc.blog.blogai.Tools.ContentRetriever;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;
import com.zhangzc.blog.blogai.Service.TLlmModelService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class WebContentRetriever implements ContentRetriever {

    private final TLlmModelService tLlmModelService;
    private QwenChatModel qwenChatModel;
    private static final String DEFAULT_PROMPT = 
            "You are a helpful assistant with internet access. " +
            "Your task is to search for information relevant to the user's query. " +
            "You MUST return the results strictly as a JSON array of strings (List<String> format). " +
            "Each string in the array should be a distinct piece of information or a summary of a search result. " +
            "Do NOT include any Markdown formatting (like ```json), explanations, or conversational text. " +
            "Just the raw JSON array (e.g., [\"result 1\", \"result 2\"]). " +
            "If no information is found, return an empty array []. " +
            "User Query: ";

    @PostConstruct
    public void init() {
        //初始化
        TLlmModel config = tLlmModelService.lambdaQuery()
                .eq(TLlmModel::getIsEnable, 1)
                .eq(TLlmModel::getIsNet, 1)
                .eq(TLlmModel::getModelType, "text")
                .one();

        if (config == null) {
            log.warn("没有配置可以联网的模型，WebContentRetriever 将不可用");
            return;
        }
        try {
            qwenChatModel = QwenChatModel.builder()
                    .apiKey(config.getApiKey())
                    .enableSearch(true)
                    .modelName(config.getModelCode())
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize QwenChatModel for WebContentRetriever", e);
        }
    }


    @Override
    public List<Content> retrieve(Query query) {
        if (qwenChatModel == null) {
            return Collections.emptyList();
        }

        String fullPrompt = DEFAULT_PROMPT + query.text();
        try {
            String response = qwenChatModel.chat(fullPrompt);
            
            // Extract JSON array from response
            int start = response.indexOf("[");
            int end = response.lastIndexOf("]");
            
            if (start != -1 && end != -1 && end > start) {
                String jsonPart = response.substring(start, end + 1);
                List<String> results = JsonUtils.parseList(jsonPart, new TypeReference<List<String>>() {});
                
                return results.stream()
                        .map(Content::from)
                        .collect(Collectors.toList());
            }
            
            log.warn("Web search response did not contain a valid JSON array: {}", response);
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("Error during web search retrieval", e);
            return Collections.emptyList();
        }
    }
}
