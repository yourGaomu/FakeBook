package com.zhangzc.booknotebiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 笔记视图对象（用于接口返回/接收笔记相关数据）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteVO {

    /**
     * 笔记ID（唯一标识，通常为 Long 类型，支持大数值场景）
     */
    private Long id;

    /**
     * 笔记内容（文本内容，可能包含长文本，用 String 类型）
     */
    private String content;

    /**
     * 用户ID（关联用户表的主键，通常为 Long 类型）
     */
    private Long userId;

    /**
     * 用户昵称（显示用，字符串类型）
     */
    private String nickname;

    /**
     * 用户头像URL（图片链接，字符串类型）
     */
    private String avatar;

    /**
     * 创建时间（推荐用 Java 8+ 新时间类型 LocalDateTime，支持时区且无线程安全问题）
     * 若接口返回格式为 String（如 "2025-10-02 15:30:00"），可暂时改为 String 类型，后续通过 JSON 工具反序列化
     */
    private LocalDateTime createTime;

    /**
     * 点赞数（统计数值，非负整数，用 Integer 类型，若数值可能超 Integer 范围可改为 Long）
     */
    private Integer likeCount;

    /**
     * 评论数（同点赞数，非负整数，用 Integer 类型）
     */
    private Integer commentCount;

    /**
     * 当前用户是否点赞（布尔值，true=已点赞，false=未点赞）
     */
    private Boolean isLiked;

    /**
     * 图片URL列表（多个图片链接，用 List<String> 存储）
     */
    private List<String> images;
}