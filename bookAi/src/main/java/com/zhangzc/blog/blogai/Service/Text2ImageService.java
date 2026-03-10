package com.zhangzc.blog.blogai.Service;

import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.community.model.dashscope.WanxImageSize;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class Text2ImageService {

    private final TLlmModelService tLlmModelService;
    private final Map<Long, ImageModel> localImageModels = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        //构建
        try {
            List<TLlmModel> list = tLlmModelService.lambdaQuery()
                    .eq(TLlmModel::getIsEnable, 1)
                    .eq(TLlmModel::getModelType, "text2Image")
                    .list();

            if (list.isEmpty()) {
                log.warn("No available image model configuration found.");
                return;
            } else {
                list.forEach(item -> {
                    ImageModel imageModel;
                    try {
                        log.info("开始构建ImageModel,模型的名称是:{}", item.getModelName());
                        if (item.getProvider().equals("aliyun")) {
// 正确的模型配置（使用 WanxImageModel + wanx 系列模型）
                            imageModel = WanxImageModel.builder()
                                    .apiKey(item.getApiKey())
                                    // 移除 baseUrl 设置！
                                    .modelName("wanx2.1-t2i-turbo") // ✅ 关键修改：使用 wanx 系列模型
                                    .size(WanxImageSize.SIZE_1024_1024)
                                    .promptExtend(true)
                                    .build();
                        } else {
                            imageModel = OpenAiImageModel.builder()
                                    .apiKey(item.getApiKey())
                                    .modelName(item.getModelCode())
                                    .logRequests(true)
                                    .logResponses(true)
                                    .build();
                        }
                        localImageModels.put(item.getId(), imageModel);
                    } catch (Exception e) {
                        log.error("构建ImageModel失败了,模型的名称是:{}", item.getModelName(), e);
                    }
                });
            }
        } catch (RuntimeException e) {
            log.error("初始化图片生成服务失败", e);
        }
    }

    /**
     * 根据提示词生成图片
     *
     * @param prompt 提示词
     * @return 图片链接
     */
    public String generateImage(String prompt) {
        return generateImage(null, prompt);
    }

    /**
     * 根据提示词生成图片
     *
     * @param modelId 模型ID (可为空)
     * @param prompt  提示词
     * @return 图片链接
     */
    public String generateImage(Long modelId, String prompt) {
        ImageModel imageModel;
        if (modelId != null) {
            imageModel = localImageModels.get(modelId);
        } else {
            // 默认取第一个可用的模型
            imageModel = localImageModels.values().stream().findFirst().orElse(null);
        }

        if (imageModel == null) {
            throw new RuntimeException("没有可用的图片生成模型");
        }

        try {
            log.info("Generating image for prompt: {}", prompt);
            Response<Image> response = imageModel.generate(prompt);
            String imageUrl = response.content().url().toString();
            log.info("Image generated successfully: {}", imageUrl);
            return imageUrl;
        } catch (Exception e) {
            log.error("Image generation failed", e);
            throw new RuntimeException("图片生成失败: " + e.getMessage());
        }
    }
}

