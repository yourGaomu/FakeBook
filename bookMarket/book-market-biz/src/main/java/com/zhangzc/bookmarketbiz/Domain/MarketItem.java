package com.zhangzc.bookmarketbiz.Domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * 二手商品实体类
 * 对应 MongoDB 集合: market_items
 */
@Data
@Document(collection = "market_items")
public class MarketItem {
    /**
     * 商品 ID
     */
    @Id
    private String id;

    /**
     * 商品标题
     * 建立全文索引，支持关键词搜索
     */
    @TextIndexed
    private String title;

    /**
     * 商品描述
     * 建立全文索引，支持关键词搜索
     */
    @TextIndexed
    private String description;

    /**
     * 价格
     */
    private Double price;

    /**
     * 原价
     */
    private Double originalPrice;

    /**
     * 图片列表
     * 第一张通常作为封面图
     */
    private List<String> images;

    /**
     * 分类
     * 例如: digital(数码), furniture(家居), clothing(服饰) 等
     */
    private String category;

    /**
     * 标签
     * 例如: ["99新", "急出", "包邮"]
     */
    private List<String> tags;

    /**
     * 地理位置 / 发货地
     */
    private String location;

    /**
     * 卖家信息 (冗余存储)
     * 避免列表页查询时进行联表操作，提高性能
     */
    private Seller seller;

    /**
     * 商品状态
     * active: 在售
     * sold: 已售
     * off_shelf: 下架
     * deleted: 已删除
     */
    private String status;

    /**
     * 浏览量
     */
    private Integer views;

    /**
     * 点赞/收藏数
     */
    private Integer likes;

    /**
     * 发布时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 卖家信息内部类
     */
    @Data
    public static class Seller {
        /**
         * 卖家用户 ID
         */
        private String id;

        /**
         * 卖家昵称
         */
        private String nickname;

        /**
         * 卖家头像 URL
         */
        private String avatar;

        /**
         * 信用分
         */
        private Integer creditScore;
    }
}
