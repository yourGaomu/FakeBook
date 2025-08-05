package com.zhangzc.bookauth.MQ.Service.Impl;

import com.zhangzc.bookauth.MQ.Service.RabbitMQService;
import com.zhangzc.bookauth.Utils.MailHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQServiceImpl implements RabbitMQService {
    private final MailHelper mailHelper;


    @Override
    public void sendCode(String to, String title, String code) {
        mailHelper.sendCode(to, title, code);
    }
}
