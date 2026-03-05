package com.zhangzc.sharethingchatimpl.consume;

import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.kafkaspringbootstart.annotation.AutoInserByRedis;
import com.zhangzc.leaf.server.service.SegmentService;
import com.zhangzc.sharethingchatimpl.domain.dto.WsMessagePacket;
import com.zhangzc.sharethingchatimpl.domain.entity.ChatConversation;
import com.zhangzc.sharethingchatimpl.domain.entity.ChatMessage;
import com.zhangzc.sharethingchatimpl.enums.ResponseCodeEnum;
import com.zhangzc.sharethingchatimpl.exception.BusinessException;
import com.zhangzc.sharethingchatimpl.repository.ChatConversationRepository;
import com.zhangzc.sharethingchatimpl.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NettyConsume {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final SegmentService segmentService;

    @KafkaListener(topics = "text")
    @AutoInserByRedis(
            strategy = AutoInserByRedis.DuplicateStrategy.SKIP, // 重复消息跳过
            enableAlert = true,                                   // 启用告警
            redisKeyPrefix = "kafka:offset"                      // Redis key前缀
    )
    /**
     * 发送的是普通文本
     * */
    public void consume(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        //开始消费
        Object value = record.value();
        if (value == null) {
            ack.acknowledge();
            return;
        }
        try {
            // KafkaUtills 发送时将对象转为了 JSON 字符串，所以这里收到的 value 是 String 类型
            if (value instanceof String json) {
                // 反序列化为 WsMessagePacket 对象
                WsMessagePacket packet = JsonUtils.parseObject(json, WsMessagePacket.class);
                log.info("收到Kafka消息: {}", packet);

                // 处理聊天消息 (Command 2)
                if (packet.getCommand() != null && packet.getMsgType() == 1) {
                    handleChatMessage(packet.getData(), packet.getMsgType());
                }

            } else {
                log.warn("收到非字符串类型的消息: {}", value.getClass());
            }
        } catch (Exception e) {
            log.error("消费消息失败", e);
        } finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "image")
    @AutoInserByRedis(
            strategy = AutoInserByRedis.DuplicateStrategy.SKIP, // 重复消息跳过
            enableAlert = true,                                   // 启用告警
            redisKeyPrefix = "kafka:offset"                      // Redis key前缀
    )
    /**
     * 发送的是图片资源
     * */
    public void consumeImage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        //开始消费
        Object value = record.value();
        if (value == null) {
            ack.acknowledge();
            return;
        }
        try {
            // KafkaUtills 发送时将对象转为了 JSON 字符串，所以这里收到的 value 是 String 类型
            if (value instanceof String json) {
                // 反序列化为 WsMessagePacket 对象
                WsMessagePacket packet = JsonUtils.parseObject(json, WsMessagePacket.class);
                log.info("收到Kafka消息: {}", packet);

                // 处理图片消息 (Command 2)
                if (packet.getCommand() != null && packet.getMsgType() == 2) {
                    handleChatMessageOnImage(packet.getData(), packet.getMsgType());
                }
            } else {
                log.warn("收到非字符串类型的消息: {}", value.getClass());
            }
        } catch (Exception e) {
            log.error("消费消息失败", e);
        } finally {
            ack.acknowledge();
        }
    }

    /**
     * 发送的是视频资源
     *
     */
    @KafkaListener(topics = "video")
    public void consumeVideo(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        //开始消费
        Object value = record.value();
        if (value == null) {
            ack.acknowledge();
            return;
        }
        try {
            // KafkaUtills 发送时将对象转为了 JSON 字符串，所以这里收到的 value 是 String 类型
            if (value instanceof String json) {
                // 反序列化为 WsMessagePacket 对象
                WsMessagePacket packet = JsonUtils.parseObject(json, WsMessagePacket.class);
                log.info("收到Kafka消息: {}", packet);

                // 处理视频消息 (Command 2)
                if (packet.getCommand() != null && packet.getMsgType() == 4) {
                    handleChatMessageOnVideo(packet.getData(), packet.getMsgType());
                }
            } else {
                log.warn("收到非字符串类型的消息: {}", value.getClass());
            }
        } catch (Exception e) {
            log.error("消费消息失败", e);
        } finally {
            ack.acknowledge();
        }
    }

    private void handleChatMessageOnVideo(Map<String, Object> data, Integer msgOriginType) {
        if (data == null) return;

        String fromUserId = (String) data.get("fromUserId");
        String toUserId = (String) data.get("toUserId");
        String content = (String) data.get("content");
        Object msgTypeObj = msgOriginType;
        Integer msgType = 4; // 默认为视频
        if (msgTypeObj instanceof Integer) {
            msgType = (Integer) msgTypeObj;
        } else if (msgTypeObj instanceof String) {
            try {
                msgType = Integer.parseInt((String) msgTypeObj);
            } catch (NumberFormatException e) {
                msgType = 4;
            }
        }

        if (fromUserId == null || toUserId == null) return;

        // 提取视频扩展信息
        Map<String, Object> extra = new HashMap<>();
        if (data.containsKey("coverUrl")) extra.put("coverUrl", data.get("coverUrl"));
        if (data.containsKey("width")) extra.put("width", data.get("width"));
        if (data.containsKey("height")) extra.put("height", data.get("height"));
        if (data.containsKey("duration")) extra.put("duration", data.get("duration"));

        // 生成会话ID
        String conversationId = getConversationId(fromUserId, toUserId);

        // 1. 保存消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId)
                .setFromUserId(fromUserId)
                .setToUserId(toUserId)
                .setMsgType(msgType)
                .setContent(content)
                .setExtra(extra) // 设置扩展信息
                .setStatus(1) // 已发送
                .setCreateTime(new Date())
                .setUpdateTime(new Date());

        // 2. 更新或创建会话
        ChatConversation conversation = chatConversationRepository.findByConversationId(conversationId)
                .orElseGet(() -> {
                    //如果没有找到
                    ChatConversation newConv = new ChatConversation();
                    newConv.setConversationId(conversationId);
                    newConv.setType(1); // 单聊
                    newConv.setMembers(Arrays.asList(fromUserId, toUserId));
                    newConv.setCreateTime(new Date());
                    newConv.setUnreadCounts(new HashMap<>());
                    return newConv;
                });

        conversation.setLastMessageContent(getPreviewContent(msgType, content));
        conversation.setLastMessageTime(chatMessage.getCreateTime());
        conversation.setUpdateTime(new Date());

        // 更新未读数 (给接收者+1)
        Map<String, Integer> unreadCounts = conversation.getUnreadCounts();
        if (unreadCounts == null) unreadCounts = new HashMap<>();
        unreadCounts.put(toUserId, unreadCounts.getOrDefault(toUserId, 0) + 1);
        conversation.setUnreadCounts(unreadCounts);

        try {
            String messageId = String.valueOf(segmentService.getId("chatMessageId").getId());
            chatMessage.setId(messageId);
            chatMessageRepository.save(chatMessage);
            conversation.setLastMessageId(chatMessage.getId());
            chatConversationRepository.save(conversation);
            log.info("已保存视频消息和会话: {}", conversationId);
        } catch (Exception e) {
            log.error("保存视频消息和会话失败: {}", conversationId, e);
            throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
        }
    }

    private void handleChatMessageOnImage(Map<String, Object> data, Integer msgOriginType) {
        if (data == null) return;

        String fromUserId = (String) data.get("fromUserId");
        String toUserId = (String) data.get("toUserId");
        String content = (String) data.get("content");
        Object msgTypeObj = msgOriginType;
        Integer msgType = 1;
        if (msgTypeObj instanceof Integer) {
            msgType = (Integer) msgTypeObj;
        } else if (msgTypeObj instanceof String) {
            try {
                msgType = Integer.parseInt((String) msgTypeObj);
            } catch (NumberFormatException e) {
                msgType = 1;
            }
        }

        if (fromUserId == null || toUserId == null) return;

        // 生成会话ID
        String conversationId = getConversationId(fromUserId, toUserId);

        // 1. 保存消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId)
                .setFromUserId(fromUserId)
                .setToUserId(toUserId)
                .setMsgType(msgType)
                .setContent(content)
                .setStatus(1) // 已发送
                .setCreateTime(new Date())
                .setUpdateTime(new Date());

        // 2. 更新或创建会话
        ChatConversation conversation = chatConversationRepository.findByConversationId(conversationId)
                .orElseGet(() -> {
                    //如果没有找到
                    ChatConversation newConv = new ChatConversation();
                    newConv.setConversationId(conversationId);
                    newConv.setType(1); // 单聊
                    newConv.setMembers(Arrays.asList(fromUserId, toUserId));
                    newConv.setCreateTime(new Date());
                    newConv.setUnreadCounts(new HashMap<>());
                    return newConv;
                });

        conversation.setLastMessageContent(getPreviewContent(msgType, content));
        conversation.setLastMessageTime(chatMessage.getCreateTime());
        conversation.setUpdateTime(new Date());

        // 更新未读数 (给接收者+1)
        Map<String, Integer> unreadCounts = conversation.getUnreadCounts();
        if (unreadCounts == null) unreadCounts = new HashMap<>();
        unreadCounts.put(toUserId, unreadCounts.getOrDefault(toUserId, 0) + 1);
        conversation.setUnreadCounts(unreadCounts);
        try {
            String messageId = String.valueOf(segmentService.getId("chatMessageId").getId());
            chatMessage.setId(messageId);
            chatMessageRepository.save(chatMessage);
            conversation.setLastMessageId(chatMessage.getId());
            chatConversationRepository.save(conversation);
            log.info("已保存消息和会话: {}", conversationId);
        } catch (Exception e) {
            log.error("保存消息和会话失败: {}", conversationId, e);
            throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
        }
    }

    private void handleChatMessage(Map<String, Object> data, Integer msgOriginType) {
        if (data == null) return;

        String fromUserId = (String) data.get("fromUserId");
        String toUserId = (String) data.get("toUserId");
        String content = (String) data.get("content");
        Object msgTypeObj = msgOriginType;
        Integer msgType = 1;
        if (msgTypeObj instanceof Integer) {
            msgType = (Integer) msgTypeObj;
        } else if (msgTypeObj instanceof String) {
            try {
                msgType = Integer.parseInt((String) msgTypeObj);
            } catch (NumberFormatException e) {
                msgType = 1;
            }
        }

        if (fromUserId == null || toUserId == null) return;

        // 生成会话ID
        String conversationId = getConversationId(fromUserId, toUserId);

        // 1. 保存消息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId)
                .setFromUserId(fromUserId)
                .setToUserId(toUserId)
                .setMsgType(msgType)
                .setContent(content)
                .setStatus(1) // 已发送
                .setCreateTime(new Date())
                .setUpdateTime(new Date());

        // 2. 更新或创建会话
        ChatConversation conversation = chatConversationRepository.findByConversationId(conversationId)
                .orElseGet(() -> {
                    //如果没有找到
                    ChatConversation newConv = new ChatConversation();
                    newConv.setConversationId(conversationId);
                    newConv.setType(1); // 单聊
                    newConv.setMembers(Arrays.asList(fromUserId, toUserId));
                    newConv.setCreateTime(new Date());
                    newConv.setUnreadCounts(new HashMap<>());
                    return newConv;
                });

        conversation.setLastMessageContent(getPreviewContent(msgType, content));
        conversation.setLastMessageTime(chatMessage.getCreateTime());
        conversation.setUpdateTime(new Date());

        // 更新未读数 (给接收者+1)
        Map<String, Integer> unreadCounts = conversation.getUnreadCounts();
        if (unreadCounts == null) unreadCounts = new HashMap<>();
        unreadCounts.put(toUserId, unreadCounts.getOrDefault(toUserId, 0) + 1);
        conversation.setUnreadCounts(unreadCounts);
        try {
            String messageId = String.valueOf(segmentService.getId("chatMessageId").getId());
            chatMessage.setId(messageId);
            chatMessageRepository.save(chatMessage);
            conversation.setLastMessageId(chatMessage.getId());
            chatConversationRepository.save(conversation);
            log.info("已保存消息和会话: {}", conversationId);
        } catch (Exception e) {
            log.error("保存消息和会话失败: {}", conversationId, e);
            throw new BusinessException(ResponseCodeEnum.SYSTEM_ERROR);
        }
    }

    private String getConversationId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    private String getPreviewContent(Integer msgType, String content) {
        if (msgType == null) return content;
        switch (msgType) {
            case 2:
                return "[图片]";
            case 3:
                return "[语音]";
            case 4:
                return "[视频]";
            default:
                return content;
        }
    }
}
