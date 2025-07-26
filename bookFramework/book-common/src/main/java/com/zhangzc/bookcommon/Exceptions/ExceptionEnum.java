package com.zhangzc.bookcommon.Exceptions;

import lombok.Getter;

@Getter
public enum ExceptionEnum implements ExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("10000", "出错啦，后台小哥正在努力修复中...");

    private String meg;
    private String code;

    ExceptionEnum(String msg, String code) {
        this.code = code;
        this.meg = msg;
    }
}

