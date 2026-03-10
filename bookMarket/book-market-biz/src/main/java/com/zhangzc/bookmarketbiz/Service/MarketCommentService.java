package com.zhangzc.bookmarketbiz.Service;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookmarketbiz.Domain.MarketComment;

public interface MarketCommentService {

    /**
     * 发布评论
     *
     * @param comment 评论信息
     * @return 评论 ID
     */
    String addComment(MarketComment comment);

    /**
     * 获取商品评论列表
     *
     * @param itemId 商品 ID
     * @param page   页码
     * @param size   每页数量
     * @return 评论列表
     */
    PageResponse<MarketComment> listComments(String itemId, int page, int size);
}
