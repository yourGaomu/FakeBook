package com.zhangzc.booksearchbiz.Pojo.Vo;

import lombok.Data;

@Data
public class PopularSearchReqVO {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
