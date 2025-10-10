package com.zhangzc.bookcountbiz.Pojo.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishCommentMqDTO {

    private Long noteId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片链接
     */
    private String imageUrl;

    /**
     * 回复的哪个评论（评论 ID）
     */
    private Long replyCommentId;

    /**
     * 发布时间
     */
    private LocalDateTime createTime;

    /**
     * 发布者 ID
     */
    private Long creatorId;

}

