package com.zhangzc.bookcommentbiz.Const;


import com.zhangzc.bookcommon.Exceptions.ExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: 犬小哈
 * @url: www.quanxiaoha.com
 * @date: 2023-08-15 10:33
 * @description: 响应异常码
 **/
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements ExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("COMMENT-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("COMMENT-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    ;

    // 异常码
    private final String Code;
    // 错误信息
    private final String Meg;

}

