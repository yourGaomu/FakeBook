package com.zhangzc.emailspringbootstart.utils;


import com.zhangzc.emailspringbootstart.consts.Email4KafkaEnum;
import com.zhangzc.emailspringbootstart.vo.MailVo;
import com.zhangzc.kafkaspringbootstart.utills.KafkaUtills;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmailUtil {

    private final KafkaUtills kafkaUtills;

    public void sendMessage(String to, String title, String content) {
        MailVo mailVo = new MailVo();
        mailVo.setTo(to);
        mailVo.setTitle(title);
        mailVo.setContent(content);
        kafkaUtills.sendMessageSync(Email4KafkaEnum.KAFKA_EMAIL_USER_ACTION.getTopic(), mailVo);
    }

}
