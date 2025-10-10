package com.zhangzc.booksearchapi.Pojo.Dto.Resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchNoteRspVO {

    /**
     * 笔记ID
     */
    //IdType.CUSTOMIZE: 由用户自定义
    private Long noteId;

    /**
     * 笔记类型：null：综合 / 0：图文 / 1：视频
     */
    //FieldStrategy.IGNORE: 忽略判断,无论字段值为什么,都会被更新
    private Integer type;

    /**
     * 封面
     */
    private String cover;


    /**
     * 话题
    * */
    private String topicName;

    /**
     * 标题
     */
    private String title;

    /**
     * 标题：关键词高亮
     */
    private String highlightTitle;

    /**
     * 发布者头像
     */
    private String avatar;

    /**
     * 发布者昵称
     */
    private String nickname;

    /**
     * 最后一次编辑时间
     */
    private LocalDateTime updateTime;

    /**
     * 被点赞总数
     */
    private Long likeTotal;

    /**
     * 被收藏总数
     *
     */
    private Long collectTotal;


    /**
     * 评论总数
     *
     */
    private Long commentTotal;
}

