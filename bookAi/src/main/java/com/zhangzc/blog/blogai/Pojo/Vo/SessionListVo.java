package com.zhangzc.blog.blogai.Pojo.Vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionListVo {
    private String sessionId;
    private String title;
    private LocalDateTime updatedAt;
    private String promptId;
}
