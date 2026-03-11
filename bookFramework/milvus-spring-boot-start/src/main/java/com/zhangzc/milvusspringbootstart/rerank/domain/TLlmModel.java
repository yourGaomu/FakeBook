package com.zhangzc.milvusspringbootstart.rerank.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * LLM Model Configuration
 * Reusing the same table structure as the chat module: t_llm_model
 */
@TableName(value ="t_llm_model")
@Data
public class TLlmModel implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Model Name (Display)
     */
    private String modelName;

    /**
     * Model Code (API)
     */
    private String modelCode;

    /**
     * Model Type (rerank, embedding, etc.)
     */
    private String modelType;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * Base URL
     */
    private String baseUrl;

    /**
     * Provider (dashscope, bge, etc.)
     */
    private String provider;

    /**
     * Is Enable 0-No 1-Yes
     */
    private Integer isEnable;

    /**
     * Created At
     */
    private Date createdAt;

    /**
     * Updated At
     */
    private Date updatedAt;

    /**
     * Is Net 0-No 1-Yes
     */
    private Integer isNet;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
