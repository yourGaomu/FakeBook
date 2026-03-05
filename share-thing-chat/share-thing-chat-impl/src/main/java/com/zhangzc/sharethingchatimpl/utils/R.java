package com.zhangzc.sharethingchatimpl.utils;


import com.zhangzc.sharethingchatimpl.enums.ResponseCodeInterface;
import lombok.Data;

@Data
public class R<T> {
    private T data;
    private String code;
    private String desc;

    public static <T> R<T> ok() {
        return new R<>();
    }

    public static <T> R<T> ok(String message) {
        R<T> r = new R<>();
        r.setDesc(message);
        r.setCode("200");
        return r;
    }

    public static <T> R<T> ok(String message, T data) {
        R<T> r = new R<>();
        r.setDesc(message);
        r.setCode("200");
        r.setData(data);
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setData(data);
        r.setCode("200");
        return r;
    }

    public static <T> R<T> error(String code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setDesc(message);
        return r;
    }

    public static <T> R<T> error(ResponseCodeInterface responseCodeEnum) {
        R<T> r = new R<>();
        r.setCode(responseCodeEnum.getCode());
        r.setDesc(responseCodeEnum.getMeg());
        return r;
    }
    public static <T> R<T> error(String message) {
        R<T> r = new R<>();
        r.setCode("500");
        r.setDesc(message);
        return r;
    }


}