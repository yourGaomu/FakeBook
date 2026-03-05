package com.zhangzc.blog.blogai.Exception;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IsEmptyForQN extends Exception{

    private String message;
    private String code;

    public IsEmptyForQN(String msg, String code) {
        this.message = msg;
        this.code = code;
    }

}
