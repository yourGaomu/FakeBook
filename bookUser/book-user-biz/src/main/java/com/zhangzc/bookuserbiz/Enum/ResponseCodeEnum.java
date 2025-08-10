package com.zhangzc.bookuserbiz.Enum;

import com.zhangzc.bookcommon.Exceptions.ExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements ExceptionInterface {

    SYSTEM_ERROR("10000", "出错啦，后台小哥正在努力修复中..."),
    USER_NOT_FOUND("USER-20000", "用户不存在"),
    ;

    private final String code;
    private final String meg;
}
