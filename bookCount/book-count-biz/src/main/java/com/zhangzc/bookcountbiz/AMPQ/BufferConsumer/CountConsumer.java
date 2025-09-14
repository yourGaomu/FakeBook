package com.zhangzc.bookcountbiz.AMPQ.BufferConsumer;

import com.zhangzc.bookcountbiz.Const.MQConstants;
import com.zhangzc.bookcountbiz.Const.RedisKeyConstants;
import com.zhangzc.bookcountbiz.Pojo.Domain.TUserCount;
import com.zhangzc.bookcountbiz.Pojo.Vo.CountFollowUnfollowMqDTO;
import com.zhangzc.bookcountbiz.Service.TUserCountService;
import com.zhangzc.bookcountbiz.Utills.RabbitMqUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CountConsumer {
    private final RedisTemplate<String, Object> redisTemplate;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RabbitMqUtil rabbitMqUtil;
    private final TUserCountService tUserCountService;

    public void consumeCountMessage(List<String> bodies) {
        log.info("当前的需要完成的消息是{}条", bodies.size());
        //开始处理，开始应该实现对象的转化,然后分类按照关注还是取关
        List<CountFollowUnfollowMqDTO> list = bodies.stream()
                .map(record -> JsonUtils.parseObject(record, CountFollowUnfollowMqDTO.class)).toList();
        // 按目标用户进行分组
        Map<Long, List<CountFollowUnfollowMqDTO>> groupMap = list.stream()
                .collect(Collectors.groupingBy(CountFollowUnfollowMqDTO::getTargetUserId));

        // 按照用户操作了多少次就可以算出用户的关注数
        Map<Long, Integer> followingCountMap = new HashMap<>();
        list.forEach(record -> {
            Long userId = record.getUserId();
            Integer type = record.getType();
            if (type == 1) {
                followingCountMap.merge(userId, 1, Integer::sum);
            } else if (type == 0) {
                followingCountMap.merge(userId, -1, Integer::sum);
            }
        });
        //进行粉丝数量的改变
        HandleFansCountMessage(groupMap);
        //进行关注量的改变
        HandleFollowingCountMessage(followingCountMap);
    }

    private void HandleFollowingCountMessage(Map<Long, Integer> followingCountMap) {
        //先存入redis里面的缓存
        followingCountMap.forEach((targetUserId, totalChange) -> {
            String redisKey = RedisKeyConstants.buildCountUserKey(targetUserId);
            // 判断 Redis 中 Hash 是否存在
            boolean isExisted = redisTemplate.hasKey(redisKey);
            if (isExisted)
                redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_FOLLOWING_TOTAL, totalChange);
        });
        //由于关注消息量没有粉丝量大则采用就用异步的方式
        //然后消息存入数据库里面,带有设置超时时间
        CompletableFuture.runAsync(() -> {
                    followingCountMap.forEach((targetUserId, totalChange) -> {
                        int maxRetries = 2;
                        int retryCount = 0;
                        while (retryCount <= maxRetries) {
                            try {
                                tUserCountService.lambdaUpdate()
                                        .eq(TUserCount::getUserId, targetUserId)
                                        .setIncrBy(TUserCount::getFollowingTotal, totalChange)
                                        .update();
                                break; // 成功则退出重试
                            } catch (Exception e) {
                                retryCount++;
                                if (retryCount > maxRetries) {
                                    log.error("数据库更新失败（重试耗尽），userId={}", targetUserId, e);
                                    return;
                                }
                                try {
                                    Thread.sleep(100 * retryCount);
                                } // 短延迟重试
                                catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                            }
                        }
                    });
                }, threadPoolTaskExecutor)
                .orTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("==> 异步任务提交失败，userId={}", followingCountMap.keySet(), ex);
                    return null;
                });
    }


    private void HandleFansCountMessage(Map<Long, List<CountFollowUnfollowMqDTO>> groupMap) {
        // key 为目标用户ID, value 为最终操作的计数
        Map<Long, Integer> countMap = new HashMap<>();
        //开始对每个进行
        groupMap.forEach((targetUserId, dtoList) -> {
            int totalChange = 0;
            for (CountFollowUnfollowMqDTO dto : dtoList) {
                Integer type = dto.getType();
                if (type == 1) {
                    totalChange += 1; // 关注：+1
                } else if (type == 0) {
                    totalChange -= 1; // 取关：-1
                }
            }
            countMap.put(targetUserId, totalChange);
        });
        log.info("## 聚合后的计数数据: {}", JsonUtils.toJsonString(countMap));
        //开始往redis里面写入数据
        countMap.forEach((targetUserId, totalChange) -> {
            // 构建 Redis Key
            String countUserKey = RedisKeyConstants.buildCountUserKey(targetUserId);
            // 判断 Redis 中 Hash 是否存在
            boolean isExisted = redisTemplate.hasKey(countUserKey);
            if (isExisted) {
                // 增加计数
                redisTemplate.opsForHash().increment(countUserKey, RedisKeyConstants.FIELD_FANS_TOTAL, totalChange);
            }
        });
        //将数据写入数据库里
        rabbitMqUtil.send("count.exchange", MQConstants.TAG_COUNT_DB, JsonUtils.toJsonString(countMap));
    }
}
