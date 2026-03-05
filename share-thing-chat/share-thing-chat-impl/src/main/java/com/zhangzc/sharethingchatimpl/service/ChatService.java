package com.zhangzc.sharethingchatimpl.service;

import com.zhangzc.sharethingchatimpl.domain.entity.ChatConversation;
import com.zhangzc.sharethingchatimpl.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;

public interface ChatService {

    /**
     * 查询历史消息 (兼容分页和游标)
     * @param userId1 用户ID 1
     * @param userId2 用户ID 2
     * @param lastCreateTime 最后一条消息的时间 (游标)，为 null 则查询最新
     * @param size 每页大小
     * @return 消息列表
     */
    List<ChatMessage> getHistoryMessages(String userId1, String userId2, Date lastCreateTime, int size);

    /**
     * 标记会话已读
     * @param userId 当前用户ID
     * @param targetUserId 对方ID
     */
    void markAsRead(String userId, String targetUserId);

    /**
     * 获取会话列表
     * @param userId 用户ID
     * @param page 页码 (0开始)
     * @param size 每页大小
     * @return 会话分页列表
     */
    Page<ChatConversation> getConversationList(String userId, int page, int size);
}
