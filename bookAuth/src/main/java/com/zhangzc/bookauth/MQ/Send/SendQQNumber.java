package com.zhangzc.bookauth.MQ.Send;

import com.zhangzc.bookauth.Const.RedisKeyConstants;
import com.zhangzc.bookauth.MQ.Service.RabbitMQService;
import com.zhangzc.bookauth.Pojo.Vo.MailVo;
import com.zhangzc.bookauth.Pojo.Vo.UserRoleByRedis;
import com.zhangzc.bookauth.Utils.RedisUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendQQNumber {

    private final RabbitMQService rabbitMQService;
    private final RedisUtil redisUtil;



    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "book.creatUserRole", declare = "true"),//不会持久化
            exchange = @Exchange(name = "book.redis", type = ExchangeTypes.TOPIC, declare = "true"
            )//topic的交换机,也不会持久化
            ,key = "createUserRoleByRedis"
    ))
    public void createUserRoleByRedis(String message) {
        threadPoolTaskExecutor.execute(() -> {
            UserRoleByRedis userRoleByRedis = JsonUtils.parseObject(message, UserRoleByRedis.class);
            //将用户的角色存入redis中
            redisUtil.set(RedisKeyConstants.buildUserRoleKey(Long.valueOf(userRoleByRedis.getUserId()))
                    , JsonUtils.toJsonString(userRoleByRedis.getRoleKeys()),-1);
        });
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "book.sendCode", declare = "true"),//不会持久化
            exchange = @Exchange(name = "book.send", type = ExchangeTypes.TOPIC, declare = "true"
            )//topic的交换机,也不会持久化
            ,key = "sendCode"
    ))
    public void sendCode(String message) {
        threadPoolTaskExecutor.execute(() -> {
            System.out.println("接受到了消息");
            System.out.println(message);
            MailVo mailVo = JsonUtils.parseObject(message, MailVo.class);
            rabbitMQService.sendCode(mailVo);
        });
    }
}
