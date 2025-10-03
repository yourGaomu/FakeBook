package com.zhangzc.booknotebiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotePageInfo {


    // 笔记基本信息
    private String id;         // 笔记ID
    private String title;      // 笔记标题
    private String content;    // 笔记内容

    /*
    - 当 type=1 时：返回用户发布的笔记
    - 当 type=2 时：返回用户收藏的笔记
    - 当 type=3 时：返回用户点赞的笔记  */
    private Integer type;
    private String cover;      // 笔记封面图片URL（当type=0时使用）
    private String videoUri;   // 视频URL（当type=1时使用）

    // 创建者信息
    private String creatorId;  // 创建者ID
    private String nickname;   // 创建者昵称
    private String avatar;     // 创建者头像URL

    // 统计信息
    private Integer likeTotal;     // 点赞数
    private Integer commentTotal;  // 评论数
    private Integer collectTotal;  // 收藏数
}
