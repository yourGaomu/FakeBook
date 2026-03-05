package com.zhangzc.sharethingchatimpl.repository;

import com.zhangzc.sharethingchatimpl.domain.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 分页查询会话历史消息 (第一页)
     */
    Page<ChatMessage> findByConversationIdOrderByCreateTimeDesc(String conversationId, Pageable pageable);

    /**
     * 游标查询历史消息 (滑动加载)
     * 查询 createTime 小于指定时间的消息
     */
    List<ChatMessage> findByConversationIdAndCreateTimeBeforeOrderByCreateTimeDesc(String conversationId, Date createTime, Pageable pageable);
}
