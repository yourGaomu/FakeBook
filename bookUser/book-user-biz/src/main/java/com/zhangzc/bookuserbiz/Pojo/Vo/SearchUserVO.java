package com.zhangzc.bookuserbiz.Pojo.Vo;

import lombok.Data;

/**
 * 用户信息实体类
 * 映射JSON中的userId、nickname、username、avatar字段
 */
@Data
public class SearchUserVO {
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户头像URL
     */
    private String avatar;
}