package com.zhangzc.sharethingchatimpl.service.impl;

import com.zhangzc.sharethingchatimpl.domain.entity.ChatConversation;
import com.zhangzc.sharethingchatimpl.domain.entity.ChatMessage;
import com.zhangzc.sharethingchatimpl.repository.ChatConversationRepository;
import com.zhangzc.sharethingchatimpl.repository.ChatMessageRepository;
import com.zhangzc.sharethingchatimpl.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatConversationRepository chatConversationRepository;

    @Override
    public List<ChatMessage> getHistoryMessages(String userId1, String userId2, Date lastCreateTime, int size) {
        String conversationId = getConversationId(userId1, userId2);
        Pageable pageable = PageRequest.of(0, size); // 永远只取第一页

        if (lastCreateTime == null) {
            // 第一页 (最新消息)
            return chatMessageRepository.findByConversationIdOrderByCreateTimeDesc(conversationId, pageable).getContent();
        } else {
            // 下一页 (基于时间游标)
            return chatMessageRepository.findByConversationIdAndCreateTimeBeforeOrderByCreateTimeDesc(conversationId, lastCreateTime, pageable);
        }
    }

    @Override
    public void markAsRead(String userId, String targetUserId) {
        String conversationId = getConversationId(userId, targetUserId);
        chatConversationRepository.findByConversationId(conversationId).ifPresent(conversation -> {
            Map<String, Integer> unreadCounts = conversation.getUnreadCounts();
            if (unreadCounts != null && unreadCounts.containsKey(userId)) {
                // 如果当前用户的未读数大于0，则清零
                if (unreadCounts.get(userId) > 0) {
                    unreadCounts.put(userId, 0);
                    conversation.setUnreadCounts(unreadCounts);
                    chatConversationRepository.save(conversation);
                    log.info("用户 {} 的会话 {} 已标记为已读", userId, conversationId);
                }
            }
        });
    }

    @Override
    public Page<ChatConversation> getConversationList(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // 查询 members 中包含 userId 的会话，并按最后消息时间倒序
        return chatConversationRepository.findByMembersContainingOrderByLastMessageTimeDesc(userId, pageable);
    }

    private String getConversationId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }
}
