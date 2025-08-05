package com.zhangzc.bookcommon.Exceptions;

import lombok.Getter;

@Getter
public enum ExceptionEnum implements ExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("10000", "出错啦，后台小哥正在努力修复中..."),
    // ----------- 业务异常状态码 -----------
    VERIFICATION_CODE_SEND_FREQUENTLY("AUTH-20000", "请求太频繁，请3分钟后再试"),
    VERIFICATION_CODE_ERROR("AUTH-20001", "验证码错误"),


    ;

    private String meg;
    private String code;

    ExceptionEnum(String msg, String code) {
        this.code = code;
        this.meg = msg;
    }
}

