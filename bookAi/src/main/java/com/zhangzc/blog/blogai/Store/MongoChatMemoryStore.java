package com.zhangzc.blog.blogai.Store;

import com.mongodb.client.result.UpdateResult;
import com.zhangzc.blog.blogai.AiService.ChatSummaryService;
import com.zhangzc.blog.blogai.Pojo.Vo.SessionListVo;
import com.zhangzc.blog.blogai.Pojo.domain.MongoChatMessage;
import com.zhangzc.blog.blogai.Pojo.domain.MongoChatSession;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MongoChatMemoryStore implements ChatMemoryStore {

    private final MongoTemplate mongoTemplate;
    private final ChatSummaryService chatSummaryService;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String sessionId = String.valueOf(memoryId);

        List<ChatMessage> result = new ArrayList<>();

        // 1. 获取 Session 信息（包含 Summary）
        MongoChatSession session = mongoTemplate.findById(sessionId, MongoChatSession.class);

        // 2. 如果有摘要，构造成 SystemMessage 插入
        if (session != null && session.getSummary() != null && !session.getSummary().isEmpty()) {
            result.add(SystemMessage.from("Summary of past conversation: " + session.getSummary()));
        }

        // 3. 获取最近的 N 条消息 (例如最近 20 条)
        Query query = new Query(Criteria.where("session_id").is(sessionId));
        query.with(Sort.by(Sort.Direction.DESC, "created_at"));
        query.limit(20);

        List<MongoChatMessage> mongoMessages = mongoTemplate.find(query, MongoChatMessage.class);

        // 4. 将消息按时间正序排列
        List<ChatMessage> recentMessages = mongoMessages.stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .map(this::toChatMessage)
                .collect(Collectors.toList());

        // 5. 确保第一条 System Message (人设) 不丢失
        // 如果 recentMessages 里没有包含初始 System Message，我们需要单独查出来加到最前面
        boolean hasSystem = recentMessages.stream().anyMatch(m -> m instanceof SystemMessage);
        if (!hasSystem) {
            MongoChatMessage firstSystem = getFirstSystemMessage(sessionId);
            if (firstSystem != null) {
                result.add(0, toChatMessage(firstSystem));
            }
        }

        result.addAll(recentMessages);
        return result;
    }

    private MongoChatMessage getFirstSystemMessage(String sessionId) {
        Query query = new Query(Criteria.where("session_id").is(sessionId).and("role").is("SYSTEM"));
        query.with(Sort.by(Sort.Direction.ASC, "created_at"));
        return mongoTemplate.findOne(query, MongoChatMessage.class);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String sessionId = String.valueOf(memoryId);

        if (messages == null || messages.isEmpty()) {
            return;
        }

        // 终极策略：
        // 1. 获取该 Session 在 DB 中的最后一条消息。
        // 2. 在传入的 messages 列表中找到这条消息的位置。
        // 3. 将该位置之后的所有消息插入 DB。
        // 4. 如果没找到（可能是新 Session，或者 DB 为空），则全量插入。
        ChatMessage lastDbMsg = getLastMessage(sessionId);
        int startIdx = 0;
        if (lastDbMsg != null) {
            String lastDbText = getText(lastDbMsg);
            // 从后往前找，找到匹配的一条
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (getText(messages.get(i)).equals(lastDbText)) {
                    startIdx = i + 1;
                    break;
                }
            }
        }

        List<ChatMessage> newMessages = messages.subList(startIdx, messages.size());
        for (ChatMessage msg : newMessages) {
            saveMessage(sessionId, msg);
        }

        updateSessionTime(sessionId);

        // 异步触发摘要生成 (简单策略：每10条触发一次)
        long totalMessages = mongoTemplate.count(new Query(Criteria.where("session_id").is(sessionId)), MongoChatMessage.class);
        if (totalMessages > 20 && totalMessages % 10 == 0) {
            triggerSummarization(sessionId);
        }
    }

    private void triggerSummarization(String sessionId) {
        // 在新线程中执行，避免阻塞当前请求
        new Thread(() -> {
            try {
                MongoChatSession session = mongoTemplate.findById(sessionId, MongoChatSession.class);
                if (session == null) return;

                // 获取最近的未摘要消息（这里简化为取最近20条作为上下文）
                Query query = new Query(Criteria.where("session_id").is(sessionId));
                query.with(Sort.by(Sort.Direction.DESC, "created_at"));
                query.limit(20);
                List<MongoChatMessage> recentMsgs = mongoTemplate.find(query, MongoChatMessage.class);

                // 按时间正序
                List<ChatMessage> messagesToSummarize = recentMsgs.stream()
                        .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                        .map(this::toChatMessage)
                        .collect(Collectors.toList());

                // 调用服务生成摘要
                String newSummary = chatSummaryService.summarize(session.getSummary(), messagesToSummarize, session.getModelId());

                // 更新 Summary
                if (newSummary != null && !newSummary.isBlank()) {
                    session.setSummary(newSummary);
                    mongoTemplate.save(session);
                }
            } catch (Exception e) {
                log.error("Error during background summarization", e);
            }
        }).start();
    }

    public List<MongoChatMessage> getHistoryDetails(String sessionId) {
        Query query = new Query(Criteria.where("session_id").is(sessionId));
        query.with(Sort.by(Sort.Direction.ASC, "created_at"));
        List<MongoChatMessage> messages = mongoTemplate.find(query, MongoChatMessage.class);
        return messages.stream()
                .filter(msg -> ChatMessageType.AI.equals(msg.getType())
                        || ChatMessageType.USER.equals(msg.getType())
                        || ChatMessageType.SYSTEM.equals(msg.getType()))
                .collect(Collectors.toList());
    }

    public List<SessionListVo> getSessionList(Long userId) {
        Query query = new Query(Criteria.where("user_id").is(userId).and("is_deleted").is(false));
        query.with(Sort.by(Sort.Direction.DESC, "updated_at"));
        List<MongoChatSession> sessions = mongoTemplate.find(query, MongoChatSession.class);
        return sessions.stream().map(s -> {
            SessionListVo vo = new SessionListVo();
            vo.setSessionId(s.getId());
            vo.setTitle(s.getTitle());
            vo.setPromptId(s.getPromptId() == null ? null : s.getPromptId().toString());
            vo.setUpdatedAt(s.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String sessionId = String.valueOf(memoryId);
        Query query = new Query(Criteria.where("_id").is(sessionId));

        Update update = new Update();
        update.set("is_deleted", true);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, MongoChatSession.class);
        if (updateResult.getModifiedCount() >= 0) {
            return;
        } else {

            log.error("Failed to delete messages for session: {}", memoryId.toString());
        }

    }

    public void createSession(String sessionId, Long userId, Long modelId, Long promptId, String title) {
        Query query = new Query(Criteria.where("id").is(sessionId));
        MongoChatSession existing = mongoTemplate.findOne(query, MongoChatSession.class);
        if (existing != null) {
            return;
        }
        MongoChatSession session = new MongoChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setModelId(modelId != null ? modelId : 1L);
        session.setPromptId(promptId);
        session.setTitle(title);
        session.setIsDeleted(false);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        mongoTemplate.save(session);
    }

    private ChatMessage getLastMessage(String sessionId) {
        Query query = new Query(Criteria.where("session_id").is(sessionId));
        query.with(Sort.by(Sort.Direction.DESC, "created_at"));
        query.limit(1);
        MongoChatMessage mongoMsg = mongoTemplate.findOne(query, MongoChatMessage.class);
        return mongoMsg != null ? toChatMessage(mongoMsg) : null;
    }

    private void saveMessage(String sessionId, ChatMessage msg) {
        MongoChatMessage doc = new MongoChatMessage();
        doc.setSessionId(sessionId);
        doc.setType(msg.type());
        doc.setContent(getText(msg));
        doc.setCreatedAt(LocalDateTime.now());
        mongoTemplate.save(doc);
    }

    private void updateSessionTime(String sessionId) {
        Query query = new Query(Criteria.where("id").is(sessionId));
        Update update = new Update().set("updated_at", LocalDateTime.now());
        mongoTemplate.updateFirst(query, update, MongoChatSession.class);
    }

    private ChatMessage toChatMessage(MongoChatMessage doc) {
        if (doc.getContent() == null) {
            return SystemMessage.from("");
        }
        switch (doc.getType()) {
            case USER:
                return UserMessage.from(doc.getContent());
            case AI:
                return AiMessage.from(doc.getContent());
            case SYSTEM:
                return SystemMessage.from(doc.getContent());
            default:
                return SystemMessage.from(doc.getContent());
        }
    }

    private String getText(ChatMessage msg) {
        if (msg instanceof UserMessage) {
            try {
                return ((UserMessage) msg).singleText();
            } catch (Exception e) {
                // Fallback for multi-modal or complex user messages
                return msg.toString();
            }
        } else if (msg instanceof AiMessage) {
            String text = ((AiMessage) msg).text();
            return text != null ? text : msg.toString();
        } else if (msg instanceof SystemMessage) {
            String text = ((SystemMessage) msg).text();
            return text != null ? text : msg.toString();
        }
        return msg.toString();
    }
}
