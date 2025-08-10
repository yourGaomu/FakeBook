package com.zhangzc.bookauth.Enum;

import com.zhangzc.bookcommon.Exceptions.ExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements ExceptionInterface {

 	// 省略...
 	LOGIN_TYPE_ERROR("AUTH-20002", "登录类型错误"),
 	USER_NOT_FOUND("AUTH-20003", "该用户不存在"),
    PHONE_OR_PASSWORD_ERROR("AUTH-20004", "手机号或密码错误"),
    ;

    // 异常码
    private final String code;
    // 错误信息
    private final String meg;

}

