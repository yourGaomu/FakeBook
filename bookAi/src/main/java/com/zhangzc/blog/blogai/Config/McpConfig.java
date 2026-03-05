//package com.zhangzc.blog.blogai.Config;
//
//import dev.langchain4j.mcp.client.DefaultMcpClient;
//import dev.langchain4j.mcp.client.McpClient;
//import dev.langchain4j.mcp.client.transport.McpTransport;
//import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.Duration;
//
//@Configuration
//public class McpConfig {
//
//    /**
//     * 创建一个连接到阿里云 MCP Server 的 McpClient Bean
//     * 注意：这里假设 MCP Server 使用 SSE 协议
//     * 你需要替换成真实的 SSE URL
//     */
//    @Bean
//    public McpClient aliyunMcpClient() {
//        // 配置 HTTP/SSE 传输层
//        McpTransport transport = new HttpMcpTransport.Builder()
//                .sseUrl("https://dashscope.aliyuncs.com/api/v1/mcps/code_interpreter_mcp/mcp") // 请替换为你的真实 URL
//                .timeout(Duration.ofSeconds(60))
//                .logRequests(true)
//                .logResponses(true)
//                .build();
//
//        // 构建 MCP Client
//        return new DefaultMcpClient.Builder()
//                .clientName("aliyun-mcp-client")
//                .transport(transport)
//                .build();
//    }
//}
