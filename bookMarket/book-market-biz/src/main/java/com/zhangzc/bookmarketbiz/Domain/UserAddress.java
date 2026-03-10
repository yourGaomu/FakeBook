package com.zhangzc.bookmarketbiz.Domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 用户收货地址实体类
 * 对应 MongoDB 集合: user_addresses
 */
@Data
@Document(collection = "user_addresses")
public class UserAddress {

    /**
     * 地址 ID
     */
    @Id
    private String id;

    /**
     * 用户 ID
     * 建立索引，方便按用户查询
     */
    @Indexed
    private Long userId;

    /**
     * 收货人姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detail;

    /**
     * 是否默认地址
     */
    private Boolean isDefault;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
