package com.zhangzc.bookrelationbiz.Enum;

import com.zhangzc.bookcommon.Exceptions.ExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements ExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("RELATION-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("RELATION-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    ;

    // 异常码
    private final String code;
    // 错误信息
    private final String meg;

}

