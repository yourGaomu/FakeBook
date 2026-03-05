package com.zhangzc.sharethingchatimpl.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements ResponseCodeInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("500", "系统繁忙，请稍后再试"),
    UNAUTHORIZED("401", "权限不足"),
    BAD_REQUEST("400", "请求参数错误"),
    AUTH_CODE_EXIT("400", "验证码已经发送"),
    AUTH_CODE_ERROR("400", "验证码错误"),
    LOGIN_PRAM_LOSS("500","参数缺少"),
    LOGIN_CODE_LOSS("501","你还没有发送过验证码"),
    LOGIN_CODE_ERROR("502", "验证码不正确"),
    USER_NOT_FOUND("404", "用户不存在"),
    RESOURCE_NOT_FOUND("513","资源未找到" ),
    FORBIDDEN("505", "你不是原作者");
    private final String code;
    private final String meg;


}

