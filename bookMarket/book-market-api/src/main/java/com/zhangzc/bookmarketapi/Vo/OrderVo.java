package com.zhangzc.bookmarketapi.Vo;

import lombok.Data;
import java.util.Date;

/**
 * 订单详情 VO
 */
@Data
public class OrderVo {
    /**
     * 订单 ID
     */
    private String id;

    /**
     * 商品快照
     */
    private ItemSnapshotVo item;

    /**
     * 买家 ID
     */
    private Long buyerId;

    /**
     * 卖家 ID
     */
    private Long sellerId;

    /**
     * 总金额
     */
    private Double totalAmount;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 商品快照 VO
     */
    @Data
    public static class ItemSnapshotVo {
        private String id;
        private String title;
        private String image;
        private Double price;
    }
}
