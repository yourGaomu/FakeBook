package com.zhangzc.blog.blogai.Tools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchRequest;
import dev.langchain4j.web.search.WebSearchResults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class TaiWebSearchTool implements WebSearchEngine {
    @Override
    @Tool("根据用户想要查找的相关学术文献进行查找,其中这个函数的返回值，将会忽略掉向量数据，只会得到具体的对象数据")
    public WebSearchResults search(String query) {
        ToolSpecification webSearchTool = ToolSpecification.builder()
                .name("web_search")
                .description("Search the internet for up-to-date information.")
                .build();
        return WebSearchEngine.super.search(query);
    }

    @Override
    @Tool("根据用户想要查找的相关学术文献进行查找,其中这个函数的返回值，将会忽略掉向量数据，只会得到具体的对象数据")
    public WebSearchResults search(WebSearchRequest webSearchRequest) {
        return null;
    }
}
