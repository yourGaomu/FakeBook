package com.zhangzc.booksearchbiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.IndexField;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.FieldType;
import org.dromara.easyes.annotation.rely.IdType;

import java.time.LocalDateTime;

/**
 * @author: 犬小哈
 * @date: 2024/4/7 15:17
 * @version: v1.0.0
 * @description: 搜索笔记
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IndexName(value = "SearchNoteInfo")
public class SearchNoteRspVO {

    /**
     * 笔记ID
     */
    @IndexId(type = IdType.CUSTOMIZE)
    //IdType.CUSTOMIZE: 由用户自定义
    // ,用户自己对id值进行set,如果用户指定的id在es中不存在
    // ,则在insert时就会新增一条记录
    // ,如果用户指定的id在es中已存在记录,则自动更新该id对应的记录
    private Long noteId;

    /**
     * 封面
     */
    private String cover;

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
    @IndexField(fieldType = FieldType.DATE, dateFormat = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    private LocalDateTime updateTime;

    /**
     * 被点赞总数
     */
    private String likeTotal;

}

