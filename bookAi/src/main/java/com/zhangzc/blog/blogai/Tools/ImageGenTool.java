package com.zhangzc.blog.blogai.Tools;

import com.zhangzc.blog.blogai.Service.Text2ImageService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageGenTool {

    private final Text2ImageService text2ImageService;

    @Tool("根据用户的文字描述生成图片")
    public String generateImage(@P("用户想要生成的图片的详细描述") String prompt) {
        log.info("Request to generate image with prompt: {}", prompt);
        try {
            return text2ImageService.generateImage(prompt);
        } catch (Exception e) {
            log.error("Failed to generate image via tool", e);
            return "图片生成失败: " + e.getMessage();
        }
    }
}
