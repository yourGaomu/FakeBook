package com.zhangzc.bookmarketbiz.Dto;

import lombok.Data;

/**
 * 用户收货地址 DTO
 * 用于添加和修改地址
 */
@Data
public class UserAddressDto {
    /**
     * 地址 ID (修改时必填)
     */
    private String id;

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
}
