package com.zhangzc.sharethingchatimpl.repository;

import com.zhangzc.sharethingchatimpl.domain.entity.ChatConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatConversationRepository extends MongoRepository<ChatConversation, String> {

    /**
     * 查询指定用户的会话列表 (按时间倒序)
     */
    Page<ChatConversation> findByMembersContainingOrderByLastMessageTimeDesc(String userId, Pageable pageable);

    /**
     * 根据会话ID查询
     */
    Optional<ChatConversation> findByConversationId(String conversationId);
}
