package com.zhangzc.booksearchbiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.HighLight;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.FieldStrategy;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.annotation.rely.IdType;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IndexName(value = "search_note_image_text_info")
public class SearchNoteRspVO {

    /**
     * 笔记ID
     */
    @IndexId(type = IdType.CUSTOMIZE)
    //IdType.CUSTOMIZE: 由用户自定义
    private Long noteId;

    /**
     * 笔记类型：null：综合 / 0：图文 / 1：视频
     */
    //FieldStrategy.IGNORE: 忽略判断,无论字段值为什么,都会被更新
    @IndexField(strategy = FieldStrategy.IGNORED)
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
    @HighLight
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
    @IndexField(fieldType = FieldType.DATE)
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

