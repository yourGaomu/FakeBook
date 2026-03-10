package com.zhangzc.blog.blogai.Pojo.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "t_chat_session")
public class MongoChatSession {

    @Id
    private String id;

    @Field("user_id")
    private Long userId;

    @Field("title")
    private String title;

    @Field("model_id")
    private Long modelId;

    @Field("prompt_id")
    private Long promptId;

    @Field("summary")
    private String summary;

    @Field("is_deleted")
    private Boolean isDeleted = false;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;
}
