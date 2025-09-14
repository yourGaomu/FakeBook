package com.zhangzc.booknotebiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteIsLikedAndCollectedRspVO {

    /**
     * 笔记 ID
     */
    private Long noteId;

    /**
     * 是否被当前登录的用户点赞
     */
    private Boolean isLiked;

    /**
     * 是否被当前登录的用户收藏
     */
    private Boolean isCollected;

}

