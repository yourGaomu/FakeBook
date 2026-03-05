package com.zhangzc.blog.blogai.Tools;

import com.zhangzc.blog.blogai.Context.AiContext4User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSearchTool {

    @Value("${tavily.api-key:tvly-xxxxx}")
    private String tavilyApiKey;

    private static final String TAVILY_SEARCH_URL = "https://api.tavily.com/search";
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool("使用搜索引擎搜索互联网上的实时信息，例如新闻、天气、股价或用户询问的未知事实。")
    public String searchWeb(@P("搜索查询关键词") String query) {
        // 检查用户是否明确禁用了联网搜索
        Boolean enableWebSearch = AiContext4User.getEnableWebSearch();
        if (enableWebSearch != null && !enableWebSearch) {
            log.info("Tool searchWeb skipped due to user preference: query={}", query);
            return "User has disabled web search for this request.";
        }

        log.info("Tool searchWeb called with query: {}", query);
        
        // 简单模拟 Tavily API 调用
        // 实际使用时需要去 https://tavily.com/ 申请 key 并配置到 application.yml
        if (tavilyApiKey == null || tavilyApiKey.contains("xxxxx")) {
            return "Web search is not configured. Please contact administrator.";
        }

        try {
            // 构建请求体
            String jsonBody = objectMapper.createObjectNode()
                    .put("api_key", tavilyApiKey)
                    .put("query", query)
                    .put("search_depth", "basic")
                    .put("include_answer", true)
                    .toString();

            // 正确的 MediaType 构造方式
            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(TAVILY_SEARCH_URL)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Tavily search failed: {}", response);
                    return "Search failed with status: " + response.code();
                }

                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                
                // 优先返回 Tavily 生成的直接答案
                if (root.has("answer") && !root.get("answer").isNull()) {
                    return root.get("answer").asText();
                }
                
                // 否则返回搜索结果摘要
                StringBuilder sb = new StringBuilder();
                if (root.has("results")) {
                    for (JsonNode result : root.get("results")) {
                        sb.append("Title: ").append(result.get("title").asText()).append("\n");
                        sb.append("Content: ").append(result.get("content").asText()).append("\n");
                        sb.append("URL: ").append(result.get("url").asText()).append("\n\n");
                    }
                }
                return sb.toString();
            }
        } catch (IOException e) {
            log.error("Error in searchWeb tool", e);
            return "Error performing web search: " + e.getMessage();
        }
    }
}
