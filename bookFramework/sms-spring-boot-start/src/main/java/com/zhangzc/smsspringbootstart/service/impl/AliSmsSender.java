package com.zhangzc.smsspringbootstart.service.impl;

import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import com.zhangzc.redisspringbootstart.utills.LuaUtil;
import com.zhangzc.redisspringbootstart.utills.RedisUtil;
import com.zhangzc.smsspringbootstart.enums.redisHashKey;
import com.zhangzc.smsspringbootstart.service.smsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AliSmsSender implements smsSender {

    private final com.aliyun.dypnsapi20170525.Client client;
    private final RedisUtil redisUtil;
    private final LuaUtil luaUtil;


    /*
     * @param signName 短信签名
     * @param templateCode 短信模板
     * @param phone 手机号
     * @param templateParam 模板参数
     * */
    public boolean sendMessage(String signName, String templateCode, String phone, String templateParam) {
        com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = new com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest()
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setPhoneNumber(phone)
                .setTemplateParam(templateParam);

        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();

        try {
            log.info("==> 开始短信发送, phone: {}, signName: {}, templateCode: {}, templateParam: {}", phone, signName, templateCode, templateParam);

            // 发送短信
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(sendSmsVerifyCodeRequest, runtime);

            log.info("==> 短信发送成功, response: {}", JsonUtils.toJsonString(response));

            CompletableFuture.runAsync(() -> {
   /*             //使用ZHash存入数据
                Map<String, Object> item = Map.of(phone, templateCode);
                boolean b = redisUtil.setHash(redisHashKey.VerificationCode, item, 300L);*/
                //
                try {
                    Map<String, String> stringStringMap = JsonUtils.parseList(templateParam, new TypeReference<Map<String, String>>() {
                    });
                    String code = stringStringMap.get("code");
                    List<Object> data = List.of(phone, code, "300");
                    Object creatAuthCode = luaUtil.execute("creat_auth_code", redisHashKey.VerificationCode, data);
                    log.info("==> lua执行结果: {}", creatAuthCode);
                } catch (Exception e) {
                    log.error("==> lua执行错误: ", e);
                }
            });
            return true;
        } catch (Exception error) {
            log.error("==> 短信发送错误: ", error);
            return false;
        }
    }

}
