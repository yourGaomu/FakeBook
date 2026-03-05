package com.zhangzc.blog.blogai.Pojo.Vo;

import lombok.Data;

@Data
public class ChatMessageVo {
    private String role; // "user", "ai", "system"
    private String content;
    private String createTime;
}
