package com.zhangzc.bookauth.Utils;

import com.zhangzc.bookauth.MQ.Send.SendQQNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MQUtil {

    private final SendQQNumber sendQQNumber;

    public void sendCode(String to,String title,String code) {
        sendQQNumber.sendCode(to, title, code);
    }
}
