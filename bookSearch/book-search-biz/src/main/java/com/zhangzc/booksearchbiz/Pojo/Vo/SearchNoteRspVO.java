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
// 指定索引名称，和你要创建的索引一致
@IndexName(value = "search_note_image_text_info")
public class SearchNoteRspVO {

    /**
     * 笔记ID
     */
    @IndexId(type = IdType.CUSTOMIZE)
    private Long noteId;

    /**
     * 笔记类型：null：综合 / 0：图文 / 1：视频
     */
    @IndexField(fieldType = FieldType.INTEGER, strategy = FieldStrategy.IGNORED)
    private Integer type;

    /**
     * 封面
     */
    @IndexField(fieldType = FieldType.KEYWORD) // 封面地址无需分词，用keyword
    private String cover;

    /**
     * 话题名称/**
     * */
    @IndexField(fieldType = FieldType.TEXT, analyzer = "ik_max_word") // 中文分词
    private String topicName;

    /**
     * 频道ID
     */
    @IndexField(fieldType = FieldType.LONG)
    private Long channelId;

    /**
     * 标题
     */
    @IndexField(fieldType = FieldType.TEXT, analyzer = "ik_max_word") // 显式声明分词器
    @HighLight()
    private String title;

    /**
     * 标题高亮结果（仅用于返回前端，不存入ES）
     */
    @IndexField() // 排除该字段，不映射到ES
    private String highlightTitle;

    /**
     * 发布者头像
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String avatar;

    /**
     * 发布者昵称
     */
    @IndexField(fieldType = FieldType.KEYWORD)
    private String nickname;

    /**
     * 最后一次编辑时间
     */
    @IndexField(fieldType = FieldType.DATE) // 显式声明时间格式
    private LocalDateTime updateTime;

    /**
     * 被点赞总数
     */
    @IndexField(fieldType = FieldType.LONG)
    private Long likeTotal;

    /**
     * 被收藏总数
     */
    @IndexField(fieldType = FieldType.LONG)
    private Long collectTotal;

    /**
     * 评论总数
     */
    @IndexField(fieldType = FieldType.LONG)
    private Long commentTotal;
}