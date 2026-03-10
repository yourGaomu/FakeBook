package com.zhangzc.bookmarketbiz.Domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 订单实体类
 * 对应 MongoDB 集合: market_orders
 */
@Data
@Document(collection = "market_orders")
public class MarketOrder {
    /**
     * 订单 ID
     */
    @Id
    private String id;

    /**
     * 商品快照
     * 记录下单时的商品信息，防止商品修改后订单信息不一致
     */
    private ItemSnapshot item;

    /**
     * 买家 ID
     */
    private Long buyerId;

    /**
     * 卖家 ID
     */
    private Long sellerId;

    /**
     * 订单总金额
     */
    private Double totalAmount;

    /**
     * 订单状态
     * pending_payment: 待支付
     * paid: 已支付/待发货
     * shipped: 已发货/待收货
     * completed: 交易完成
     * cancelled: 已取消
     * refunded: 已退款
     */
    private String status;

    /**
     * 收货地址信息
     */
    private ShippingAddress shippingAddress;

    /**
     * 订单备注/留言
     */
    private String remark;

    /**
     * 创建时间 (下单时间)
     */
    private Date createdAt;

    /**
     * 支付时间
     */
    private Date paidAt;

    /**
     * 发货时间
     */
    private Date shippedAt;

    /**
     * 完成时间
     */
    private Date completedAt;

    /**
     * 商品快照内部类
     */
    @Data
    public static class ItemSnapshot {
        /**
         * 商品 ID
         */
        private String id;

        /**
         * 商品标题
         */
        private String title;

        /**
         * 商品封面图
         */
        private String image;

        /**
         * 成交价格
         */
        private Double price;
    }

    /**
     * 收货地址内部类
     */
    @Data
    public static class ShippingAddress {
        /**
         * 收货人姓名
         */
        private String name;

        /**
         * 联系电话
         */
        private String phone;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
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
    }
}
