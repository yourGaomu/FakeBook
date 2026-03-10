package com.zhangzc.smsspringbootstart.utills;

import com.zhangzc.smsspringbootstart.service.impl.AliSmsSender;
import com.zhangzc.smsspringbootstart.service.smsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
public class SmsUtil {
    private final smsSender aliSmsSender;

    public boolean sendSms(String signName, String templateCode, String phone, String templateParam) {
        return aliSmsSender.sendMessage(signName, templateCode, phone, templateParam);
    }
}
