package com.zhangzc.blog.blogai.Exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IsNoRole extends Exception{
    private String message;
    private String code;

    public IsNoRole(String msg, String code) {
        this.message = msg;
        this.code = code;
    }
}
