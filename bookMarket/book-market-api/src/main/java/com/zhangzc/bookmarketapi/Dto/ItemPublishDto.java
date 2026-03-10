package com.zhangzc.bookmarketapi.Dto;

import lombok.Data;
import java.util.List;

/**
 * 商品发布 DTO
 */
@Data
public class ItemPublishDto {
    /**
     * 商品标题
     */
    private String title;

    /**
     * 出售价格
     */
    private Double price;

    /**
     * 原价
     */
    private Double originalPrice;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 图片 URL 列表
     */
    private List<String> images;

    /**
     * 分类
     */
    private String category;

    /**
     * 发货地/位置
     */
    private String location;

    /**
     * 标签列表
     */
    private List<String> tags;
}
