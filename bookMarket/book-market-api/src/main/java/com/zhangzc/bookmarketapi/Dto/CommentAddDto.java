package com.zhangzc.bookmarketapi.Dto;

import lombok.Data;

@Data
public class CommentAddDto {
    /**
     * 商品 ID
     */
    private String itemId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论 ID (可选)
     */
    private String parentId;
    
    /**
     * 被回复用户ID (可选)
     */
    private Long replyToUserId;
}
