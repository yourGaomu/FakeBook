package com.zhangzc.bookauth.Utils;


import com.zhangzc.bookauth.Pojo.Vo.MailVo;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MQUtil {

    private final RabbitTemplate rabbitTemplate;
    private final MailHelper mailHelper;
    public void sendCode(String to, String title, String code) {
        rabbitTemplate.convertAndSend("book.send","sendCode", JsonUtils.toJsonString(MailVo.builder().to(to).title(title).content(code).build()));
       /* mailHelper.sendCode(MailVo.builder().to(to).title(title).content(code).build());*/
    }

    public void send(String exchange,String routeKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routeKey, message);
    }
}
