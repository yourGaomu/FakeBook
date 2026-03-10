package com.zhangzc.bookmarketbiz.Consumer;

import com.zhangzc.bookmarketbiz.Domain.MarketComment;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.kafkaspringbootstart.annotation.AutoInserByRedis;
import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MarketCommentConsumer {

    private static final String COMMENT_CACHE_PREFIX = "product:comment:";
    private final RedisUtil redisUtil;

    @KafkaListener(topics = "market-comment-topic", groupId = "market-comment-group")
    @AutoInserByRedis(strategy = AutoInserByRedis.DuplicateStrategy.SKIP, enableAlert = true, redisKeyPrefix = "kafka:offset:market-comment")
    public void onMessage(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        try {
            Object value = record.value();
            MarketComment marketComment = null;
            if (value instanceof String jsonStr) {
                // 尝试解析为 ChatHistoryDto
                try {
                    marketComment  = JsonUtils.parseObject(jsonStr, MarketComment.class);
                } catch (Exception e) {
                    log.warn("Direct JSON parsing failed: {}", jsonStr, e);
                }
            } else if (value instanceof MarketComment) {
                marketComment = (MarketComment) value;
            } else {
                // 如果是LinkedHashMap等其他类型（Spring Kafka默认JsonDeserializer可能转换成Map）
                try {
                    String json = JsonUtils.toJsonString(value);
                    marketComment = JsonUtils.parseObject(json, MarketComment.class);
                } catch (Exception e) {
                    log.warn("Failed to convert value to ChatHistoryDto: {}", value.getClass(), e);
                }
            }
            if (marketComment != null && marketComment.getItemId() != null) {
                String cacheKey = COMMENT_CACHE_PREFIX + marketComment.getItemId();
                // 使用 ZADD 更新缓存
                // 分数为创建时间戳
                redisUtil.zAdd(cacheKey, marketComment, (double) marketComment.getCreateTime().getTime());
                log.info("Updated redis cache for item: {}", marketComment.getItemId());
            }
        } catch (Exception e) {
            log.error("Failed to process market comment message", e);
        }
    }
}
