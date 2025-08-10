package com.zhangzc.bookauth.MQ.Service;

public interface RabbitMQService {

    public void sendCode(String to, String title, String code);
}
