package com.zhangzc.blog.blogai.Pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * LLM模型配置表
 * @TableName t_llm_model
 */
@TableName(value ="t_llm_model")
@Data
public class TLlmModel implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称(展示用)
     */
    private String modelName;

    /**
     * 模型代码(API调用用)
     */
    private String modelCode;

    /**
     * 模型类型(text, multimodal, embedding, vision, text2image, text2video 等)
     */
    private String modelType;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API Base URL
     */
    private String baseUrl;

    /**
     * 提供商(openai, aliyun等)
     */
    private String provider;

    /**
     * 关联的系统提示词ID
     */
    private Long systemMessageId;

    /**
     * 是否启用 0-否 1-是
     */
    private Integer isEnable;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 是否原生支持联网 0-否 1-是
     */
    private Integer isNet;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
