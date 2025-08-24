package com.zhangzc.bookauth.Pojo.Vo;

import lombok.Data;

import java.util.List;

@Data
public class UserRoleByRedis {
    private Long userId;
    private List<String> roleKeys;
}
