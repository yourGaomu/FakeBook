package com.zhangzc.bookauth.Service.impl;


import com.zhangzc.bookauth.Const.RedisKeyConstants;
import com.zhangzc.bookauth.Service.VerificationCodeService;
import com.zhangzc.bookauth.Utils.MQUtil;
import com.zhangzc.bookauth.Utils.MailHelper;
import com.zhangzc.bookauth.Utils.RedisUtil;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Exceptions.ExceptionEnum;
import com.zhangzc.bookcommon.Utils.R;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final RedisUtil redisTemplate;
    private final MQUtil mqUtil;

    /**
     * 发送短信验证码
     *
     * @param sendVerificationCodeReqVO
     * @return
     */
    @Override
    public R send(Map<String,String> sendVerificationCodeReqVO) throws BizException {
        // QQ号
        String phone = sendVerificationCodeReqVO.get("phone");

        // 构建验证码 redis key
        String key = RedisKeyConstants.buildVerificationCodeKey(phone);

        // 判断是否已发送验证码
        boolean isSent = redisTemplate.hasKey(key);
        if (isSent) {
            // 若之前发送的验证码未过期，则提示发送频繁
            throw new BizException(ExceptionEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }

        // 生成 6 位随机数字验证码
        String verificationCode = RandomUtil.randomNumbers(6);

        // todo: 调用第三方短信发送服务
        mqUtil.sendCode(phone,"验证码",verificationCode);

        log.info("==> 手机号: {}, 已发送验证码：【{}】", phone, verificationCode);

        // 存储验证码到 redis, 并设置过期时间为 3 分钟
        redisTemplate.set(key,verificationCode,60*3);

        return R.success();
    }
}

