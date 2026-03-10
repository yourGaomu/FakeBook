package com.zhangzc.bookmarketapi.Vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 商品详情 VO
 */
@Data
public class MarketItemVo {
    /**
     * 商品 ID
     */
    private String id;

    /**
    * 状态
    * */
    private Boolean isLike;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
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
     */
    private List<String> images;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 卖家信息
     */
    private SellerVo seller;

    /**
     * 状态
     */
    private String status;

    /**
     * 浏览量
     */
    private Integer views;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 发布时间
     */
    private Date createdAt;


    /**
     * 卖家信息 VO
     */
    @Data
    public static class SellerVo {
        private Long id;
        private String nickname;
        private String avatar;
        private Integer creditScore;
    }
}
