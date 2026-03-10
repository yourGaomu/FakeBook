package com.zhangzc.bookmarketapi.Dto;

import lombok.Data;

/**
 * 订单创建 DTO
 */
@Data
public class OrderCreateDto {
    /**
     * 购买的商品 ID
     */
    private String itemId;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 收货地址 ID (预留)
     */
    private Long addressId;
}
