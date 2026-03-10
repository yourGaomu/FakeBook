package com.zhangzc.bookmarketapi.Dto;

import lombok.Data;

/**
 * 商品查询条件 DTO
 */
@Data
public class ItemQueryDto {
    /**
     * 页码，默认 1
     */
    private Integer page = 1;

    /**
     * 每页数量，默认 20
     */
    private Integer pageSize = 20;

    /**
     * 分类筛选
     * 可选值: digital, furniture, clothing, game, camera, other
     * "all" 或空表示不限
     */
    private String category;

    /**
     * 搜索关键词
     * 匹配标题和描述
     */
    private String keyword;
}
