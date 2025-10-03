package com.zhangzc.booknotebiz.Pojo.Vo;


import lombok.Builder;
import lombok.Data;

/**
 * 频道分页请求参数类（用于接收“指定频道+分页”的查询条件）
 */
@Data
@Builder
public class ChannelPageRequest {

    /**
     * 频道ID（频道唯一标识，通常为字符串格式，如 UUID 或业务自定义ID）
     * @NotBlank：参数校验注解，确保请求时该字段不为空（空字符串、纯空格也会被拦截）
     */

    private String channelId;

    /**
     * 页码数字（分页查询的页码，从 1 开始，需为正整数）
     * @Positive：参数校验注解，确保页码为大于 0 的整数（避免传入 0 或负数）
     */

    private Integer pageNo;
}