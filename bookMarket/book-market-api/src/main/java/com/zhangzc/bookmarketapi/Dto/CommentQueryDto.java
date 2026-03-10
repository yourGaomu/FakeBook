package com.zhangzc.bookmarketapi.Dto;

import lombok.Data;

@Data
public class CommentQueryDto {
    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 20;
}
