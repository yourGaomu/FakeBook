package com.zhangzc.bookrelationbiz.Amqp;


import com.google.common.util.concurrent.RateLimiter;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.bookrelationbiz.Const.MQConstants;
import com.zhangzc.bookrelationbiz.Enum.ResponseCodeEnum;
import com.zhangzc.bookrelationbiz.Pojo.Domain.TFans;
import com.zhangzc.bookrelationbiz.Pojo.Domain.TFollowing;
import com.zhangzc.bookrelationbiz.Pojo.Dto.FollowUserMqDTO;
import com.zhangzc.bookrelationbiz.Service.TFansService;
import com.zhangzc.bookrelationbiz.Service.TFollowingService;
import com.zhangzc.bookrelationbiz.Utils.RedisUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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


    //负载均衡
    @RabbitListener(queues = "relation.queue")
    public void consumeDelayMessageQueue2(String message) {
        try {
            FollowUserMqDTO followUserMqDTO = JsonUtils.parseObject(message, FollowUserMqDTO.class);
            String tag = followUserMqDTO.getTag();
            // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
            rateLimiter.acquire();
            switch (tag) {
                case "Follow":
                    handleFollowTagMessage(followUserMqDTO);
                    break;
                case "Unfollow":
                    break;
                default:
                    throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //_______________________________________________________________________________________________________

    @Transactional(rollbackFor = Exception.class)
    protected void handleFollowTagMessage(FollowUserMqDTO followUserMqDTO) {
        //关注表加入数据
        tFollowingService.save(TFollowing.builder()
                .userId(followUserMqDTO.getUserId())
                .followingUserId(followUserMqDTO.getFollowUserId())
                .createTime(TimeUtil.getDateTime(followUserMqDTO.getCreateTime()))
                .build());
        //粉丝表加入数据
        tFansService.save(TFans.builder()
                .userId(followUserMqDTO.getFollowUserId())
                .fansUserId(followUserMqDTO.getUserId())
                .createTime(TimeUtil.getDateTime(followUserMqDTO.getCreateTime()))
                .build());

    }


    //__________________________________________________________________________________________________________

    @RabbitListener(queues = "relation.queue")
    public void consumeDelayMessageQueue(String message) {
        System.out.println("接受到了消息2");
    }


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
    public void consumeDelayMessage(String message) {
    }

}
