package com.zhangzc.blog.blogai.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiMessage {
    private String message;
    private String qq;
    private Long modelId;
    @Nullable
    private String sessionId;
    @Nullable
    private Long visionModelId;
    @Nullable
    private String imageUrl;
    
    /**
     * 是否开启联网搜索 (用户侧开关)
     * null/false: 不强制，默认由系统决定
     * true: 强制开启 (对于原生不支持的模型，调用 Tool；对于原生支持的模型，通过 Prompt 提示)
     */
    @Nullable
    private Boolean enableWebSearch;
}
