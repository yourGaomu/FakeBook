package com.zhangzc.bookcommon.Utils;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Exceptions.ExceptionInterface;

import lombok.Data;

@Data
public class R<T> {
    // 是否成功，默认为 true
    private boolean success = true;
    // 响应消息
    private String message;
    // 异常码
    private String errorCode;
    // 响应数据
    private T data;

    // =================================== 成功响应 ===================================
    public static <T> R<T> success() {

        R<T> response = new R<>();
        return response;
    }

    public static <T> R<T> success(T data) {
        R<T> response = new R<>();
        response.setData(data);
        return response;
    }

    // =================================== 失败响应 ===================================
    public static <T> R<T> fail() {
        R<T> response = new R<>();
        response.setSuccess(false);
        return response;
    }

    public static <T> R<T> fail(String errorMessage) {
        R<T> response = new R<>();
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }

    public static <T> R<T> fail(String errorCode, String errorMessage) {
        R<T> response = new R<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(errorMessage);
        return response;
    }

    public static <T> R<T> fail(BizException bizException) {
        R<T> response = new R<>();
        response.setSuccess(false);
        response.setErrorCode(bizException.getCode());
        response.setMessage(bizException.getMessage());
        return response;
    }

    public static <T> R<T> fail(ExceptionInterface baseExceptionInterface) {
        R<T> response = new R<>();
        response.setSuccess(false);
        response.setErrorCode(baseExceptionInterface.getCode());
        response.setMessage(baseExceptionInterface.getMeg());
        return response;
    }

}
