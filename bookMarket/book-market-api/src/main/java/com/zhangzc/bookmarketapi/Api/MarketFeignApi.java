package com.zhangzc.bookmarketapi.Api;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookmarketapi.Dto.ItemPublishDto;
import com.zhangzc.bookmarketapi.Dto.ItemQueryDto;
import com.zhangzc.bookmarketapi.Dto.OrderCreateDto;
import com.zhangzc.bookmarketapi.Vo.MarketItemVo;
import com.zhangzc.bookmarketapi.Vo.OrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 二手交易市场 Feign 接口
 * 提供商品管理、订单创建、互动等功能
 */
@FeignClient(name = "book-market-biz", path = "/market")
public interface MarketFeignApi {

    /**
     * 获取商品列表
     * 支持分页、关键词搜索、分类筛选
     *
     * @param queryDto 查询条件 DTO
     * @return 分页的商品列表
     */
    @PostMapping("/items/list")
    PageResponse<MarketItemVo> listItems(@RequestBody ItemQueryDto queryDto);

    /**
     * 获取商品详情
     *
     * @param id 商品 ID
     * @return 商品详情 VO
     */
    @PostMapping("/items/detail/{id}")
    R<MarketItemVo> getItemDetail(@PathVariable("id") String id);

    /**
     * 发布闲置商品
     *
     * @param publishDto 发布信息 DTO
     * @return 发布的商品 ID
     */
    @PostMapping("/items/publish")
    R<String> publishItem(@RequestBody ItemPublishDto publishDto);

    /**
     * 创建订单/购买商品
     *
     * @param orderDto 订单创建 DTO
     * @return 订单详情 VO
     */
    @PostMapping("/orders/create")
    R<OrderVo> createOrder(@RequestBody OrderCreateDto orderDto);

    /**
     * 点赞/取消点赞商品
     *
     * @param id 商品 ID
     * @return 点赞结果（是否点赞，当前点赞数）
     */
    @PostMapping("/items/like/{id}")
    R<Object> likeItem(@PathVariable("id") String id);
}
