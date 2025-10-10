package com.zhangzc.bookcommentbiz.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum CommentLevelEnum {
    // 一级评论
    ONE(1),
    // 二级评论
    TWO(2),
    ;

    private final Integer code;

}

