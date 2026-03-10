package com.zhangzc.bookmarketbiz.Vo;

import lombok.Data;
import java.util.Date;

/**
 * 用户收货地址 VO
 * 用于返回给前端
 */
@Data
public class UserAddressVo {
    /**
     * 地址 ID
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

    /**
     * 创建时间
     */
    private Date createdAt;
}
