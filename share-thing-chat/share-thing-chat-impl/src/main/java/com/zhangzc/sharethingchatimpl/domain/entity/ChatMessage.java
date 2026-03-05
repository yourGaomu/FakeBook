package com.zhangzc.sharethingchatimpl.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

/**
 * 聊天消息实体
 */
@Data
@Accessors(chain = true)
@Document(collection = "chat_message")
@CompoundIndex(def = "{'conversationId': 1, 'createTime': -1}") // 复合索引，用于查询历史记录
public class ChatMessage {

    @Id
    private String id;

    /**
     * 会话ID (单聊: smallUserId_bigUserId, 群聊: groupId)
     */
    @Indexed
    private String conversationId;

    /**
     * 发送者ID
     */
    private String fromUserId;

    /**
     * 接收者ID (单聊为对方ID，群聊为群ID)
     */
    private String toUserId;

    /**
     * 消息类型
     * 1: 文本
     * 2: 图片
     * 3: 语音
     * 4: 视频
     * 100: 系统消息
     */
    private Integer msgType;

    /**
     * 消息内容 (文本内容 或 媒体URL)
     */
    private String content;

    /**
     * 扩展信息 (JSON结构，存储图片宽高、语音时长、@列表等)
     */
    private Map<String, Object> extra;

    /**
     * 消息状态
     * 0: 发送中
     * 1: 已发送
     * 2: 已读
     * 3: 已撤回
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
