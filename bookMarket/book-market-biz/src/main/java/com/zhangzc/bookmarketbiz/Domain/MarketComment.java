package com.zhangzc.bookmarketbiz.Domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 商品留言/评论实体类
 * 对应 MongoDB 集合: market_comments
 */
@Data
@Document(collection = "market_comments")
@CompoundIndexes({
    @CompoundIndex(name = "ItemCommentsPageableIndex", def = "{'itemId': 1, 'createTime': -1}"),
    @CompoundIndex(name = "UserCommentsIndex", def = "{'userId': 1, 'createTime': -1}")
})
public class MarketComment {
    /**
     * 评论 ID
     */
    @Id
    private String id;

    /**
     * 关联的商品 ID
     */
    private String itemId;

    /**
     * 评论用户 ID
     */
    private Long userId;

    /**
     * 评论用户昵称 (冗余存储)
     */
    private String userNickname;

    /**
     * 评论用户头像 (冗余存储)
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论 ID (如果是回复)
     */
    private String parentId;

    /**
     * 被回复的用户 ID
     */
    private Long replyToUserId;

    /**
     * 评论时间
     */
    private Date createTime;
}
