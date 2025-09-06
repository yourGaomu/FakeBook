package com.zhangzc.bookrelationbiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFansListReqVO {

    private Long userId;


    private Integer pageNo = 1; // 默认值为第一页
}

