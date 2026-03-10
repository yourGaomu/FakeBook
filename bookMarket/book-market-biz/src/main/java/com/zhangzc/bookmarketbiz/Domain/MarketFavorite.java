package com.zhangzc.bookmarketbiz.Domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 用户收藏/点赞实体类
 * 对应 MongoDB 集合: market_favorites
 * 设置了复合唯一索引 (userId + itemId)，防止重复收藏
 */
@Data
@Document(collection = "market_favorites")
@CompoundIndex(name = "unique_favorite", def = "{'userId': 1, 'itemId': 1}", unique = true)
public class MarketFavorite {
    /**
     * 主键 ID
     */
    @Id
    private String id;

    /**
     * 用户 ID
     */
    @org.springframework.data.mongodb.core.index.Indexed
    private Long userId;

    /**
     * 商品 ID
     */
    @org.springframework.data.mongodb.core.index.Indexed
    private String itemId;

    /**
     * 收藏时间
     */
    private Date createdAt;
}
