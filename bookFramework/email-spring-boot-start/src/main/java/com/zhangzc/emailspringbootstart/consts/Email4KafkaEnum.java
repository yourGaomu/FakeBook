package com.zhangzc.emailspringbootstart.consts;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum Email4KafkaEnum {
    KAFKA_EMAIL_USER_ACTION("email.user.action");
    private final String Topic;

}
