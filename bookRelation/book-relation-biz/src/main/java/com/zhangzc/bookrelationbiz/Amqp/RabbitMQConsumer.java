package com.zhangzc.bookrelationbiz.Amqp;


import com.google.common.util.concurrent.RateLimiter;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.bookrelationbiz.Const.MQConstants;
import com.zhangzc.bookrelationbiz.Const.RedisKeyConstants;
import com.zhangzc.bookrelationbiz.Enum.ResponseCodeEnum;
import com.zhangzc.bookrelationbiz.Pojo.Domain.TFans;
import com.zhangzc.bookrelationbiz.Pojo.Domain.TFollowing;
import com.zhangzc.bookrelationbiz.Pojo.Dto.FollowUserMqDTO;
import com.zhangzc.bookrelationbiz.Pojo.Vo.CountFollowUnfollowMqDTO;
import com.zhangzc.bookrelationbiz.Service.TFansService;
import com.zhangzc.bookrelationbiz.Service.TFollowingService;
import com.zhangzc.bookrelationbiz.Utils.DateUtils;
import com.zhangzc.bookrelationbiz.Utils.RabbitMqUtil;
import com.zhangzc.bookrelationbiz.Utils.RedisUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisUtil redisUtil;
    private final TFollowingService tFollowingService;
    private final TFansService tFansService;
    private final RateLimiter rateLimiter;
    private final RedisTemplate redisTemplate;
    private final RabbitMqUtil rabbitMqUtil;


    //负载均衡
    @RabbitListener(queues = "relation.queue")
    @Transactional(rollbackFor = Exception.class)
    public void consumeMessageQueue2(String message) {
        try {
            FollowUserMqDTO followUserMqDTO = JsonUtils.parseObject(message, FollowUserMqDTO.class);
            String tag = followUserMqDTO.getTag();
            CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = new CountFollowUnfollowMqDTO();
            // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
            rateLimiter.acquire();
            switch (tag) {
                case "Follow":
                    //处理关注的消息
                    handleFollowTagMessage(followUserMqDTO);
                    countFollowUnfollowMqDTO.setType(1);
                    countFollowUnfollowMqDTO.setUserId(followUserMqDTO.getUserId());
                    countFollowUnfollowMqDTO.setTargetUserId(followUserMqDTO.getFollowUserId());
                    break;
                case "Unfollow":
                    handleUnfollowTagMessage(followUserMqDTO);
                    countFollowUnfollowMqDTO.setType(0);
                    countFollowUnfollowMqDTO.setUserId(followUserMqDTO.getUserId());
                    countFollowUnfollowMqDTO.setTargetUserId(followUserMqDTO.getFollowUserId());
                    break;
                default:
                    throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
            }

            //发送计数服务来聚合减少操作io
            rabbitMqUtil.send("count.exchange", MQConstants.TAG_COUNT,
                    JsonUtils.toJsonString(countFollowUnfollowMqDTO));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    protected void handleUnfollowTagMessage(FollowUserMqDTO followUserMqDTO) {

        Long userId = followUserMqDTO.getUserId();
        Long unfollowUserId = followUserMqDTO.getFollowUserId();
        LocalDateTime createTime = followUserMqDTO.getCreateTime();

        // 取关成功需要删除数据库两条记录
        // 关注表：一条记录
        tFollowingService.removeById(
                tFollowingService.lambdaQuery()
                        .eq(TFollowing::getUserId, userId)
                        .eq(TFollowing::getFollowingUserId, unfollowUserId)
                        .one());
        // 粉丝表：一条记录
        tFansService.removeById(
                tFansService.lambdaQuery()
                        .eq(TFans::getUserId, unfollowUserId)
                        .eq(TFans::getFansUserId, userId)
                        .one());
        // 被取关用户的粉丝列表 Redis Key
        String fansRedisKey = RedisKeyConstants.buildUserFansKey(unfollowUserId);
        // 删除指定粉丝
        redisTemplate.opsForZSet().remove(fansRedisKey, userId);
    }


//_______________________________________________________________________________________________________

    @Transactional(rollbackFor = Exception.class)
    protected void handleFollowTagMessage(FollowUserMqDTO followUserMqDTO) throws BizException {
        //关注表加入数据
        boolean save = tFollowingService.save(
                TFollowing.builder()
                        .userId(followUserMqDTO.getUserId())
                        .followingUserId(followUserMqDTO.getFollowUserId())
                        .createTime(TimeUtil.getDateTime(followUserMqDTO.getCreateTime()))
                        .build());
        if (!save) {
            throw new BizException(ResponseCodeEnum.SYSTEM_ERROR);
        }
        //粉丝表加入数据
        boolean save1 = tFansService.save(TFans.builder()
                .userId(followUserMqDTO.getFollowUserId())
                .fansUserId(followUserMqDTO.getUserId())
                .createTime(TimeUtil.getDateTime(followUserMqDTO.getCreateTime()))
                .build());
        if (!save1) {
            throw new BizException(ResponseCodeEnum.SYSTEM_ERROR);
        }

        // Lua 脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
        script.setResultType(Long.class);

        // 时间戳
        long timestamp = DateUtils.localDateTime2Timestamp(followUserMqDTO.getCreateTime());

        // 构建被关注用户的粉丝列表 Redis Key
        String fansRedisKey = RedisKeyConstants.buildUserFansKey(followUserMqDTO.getFollowUserId());
        // 执行脚本
        redisTemplate.execute(script, Collections.singletonList(fansRedisKey), followUserMqDTO.getUserId(), timestamp);

        //todo 发送 MQ 通知计数服务：统计关注数
    }


//__________________________________________________________________________________________________________


    @RabbitListener(bindings = {  // 注意：多个绑定关系用 {} 包裹，逗号分隔
            // 第一个队列绑定
            @QueueBinding(
                    value = @Queue(
                            name = "relation.queue",   // 队列1名称
                            durable = "true"        // 持久化
                    ),
                    exchange = @Exchange(
                            name = "relation.exchange",         // 同一个延迟交换机
                            type = ExchangeTypes.TOPIC,      // 延迟交换机类型
                            durable = "true"
                    ),
                    key = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW  // 相同的路由键
            )
    })
    public void consumeRelationMessage(String message) {
    }





}
