package com.zhangzc.bookcommentbiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentItemRspVO {

    /**
     * 评论 ID
     */
    private Long commentId;

    /**
     * 发布者用户 ID
     */
    private Long userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片
     */
    private String imageUrl;

    /**
     * 发布时间
     */
    private String createTime;

    /**
     * 被点赞数
     */
    private Long likeTotal;

    /**
     * 二级评论总数
     */
    private Long childCommentTotal;

    /**
     * 最早回复的评论
     */
    private FindCommentItemRspVO firstReplyComment;

}

