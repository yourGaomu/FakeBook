package com.zhangzc.sharethingchatimpl.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 会话列表实体 (Inbox)
 */
@Data
@Accessors(chain = true)
@Document(collection = "chat_conversation")
public class ChatConversation {

    @Id
    private String id;

    /**
     * 会话ID (唯一标识)
     */
    @Indexed(unique = true)
    private String conversationId;

    /**
     * 会话类型
     * 1: 单聊
     * 2: 群聊
     */
    private Integer type;

    /**
     * 会话成员ID列表
     */
    @Indexed
    private List<String> members;

    /**
     * 最后一条消息ID
     */
    private String lastMessageId;

    /**
     * 最后一条消息内容预览
     */
    private String lastMessageContent;

    /**
     * 最后一条消息时间 (用于排序)
     */
    @Indexed
    private Date lastMessageTime;

    /**
     * 各成员未读数 (Key: UserId, Value: Count)
     */
    private Map<String, Integer> unreadCounts;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
