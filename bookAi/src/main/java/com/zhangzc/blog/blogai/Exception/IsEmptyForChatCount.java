package com.zhangzc.blog.blogai.Exception;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IsEmptyForChatCount extends Exception{
    private String message;
    private String code;

    public IsEmptyForChatCount(String code,String message) {
        this.message = message;
        this.code = code;
    }
}
