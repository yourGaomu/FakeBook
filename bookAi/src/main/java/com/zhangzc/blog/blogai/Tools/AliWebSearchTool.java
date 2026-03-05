package com.zhangzc.blog.blogai.Tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class AliWebSearchTool {
    @Tool("Search the internet for up-to-date information.")
    public String web_search(String query) {
        // 这个方法体永远不会被执行！
        // 因为百炼会拦截 tool call 并在服务端执行搜索
        throw new UnsupportedOperationException("This method is not meant to be called locally.");
    }
}
