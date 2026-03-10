package com.zhangzc.bookmarketbiz.Controller;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookmarketapi.Dto.*;
import com.zhangzc.bookmarketapi.Vo.MarketItemVo;
import com.zhangzc.bookmarketapi.Vo.OrderVo;
import com.zhangzc.bookmarketbiz.Domain.MarketComment;
import com.zhangzc.bookmarketbiz.Service.MarketCommentService;
import com.zhangzc.bookmarketbiz.Service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 二手交易市场控制器
 */
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;
    private final MarketCommentService marketCommentService;


    /**
     * 获取商品列表接口
     *
     * @param queryDto 查询参数
     * @return 商品列表
     */
    @PostMapping("/items/list")
    public PageResponse<MarketItemVo> listItems(@RequestBody ItemQueryDto queryDto) {
        return marketService.listItems(queryDto);
    }

    /**
     * 获取商品详情接口
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @PostMapping("/items/detail/{id}")
    public R<MarketItemVo> getItemDetail(@PathVariable("id") String id) throws BizException {
        return R.success(marketService.getItemDetail(id));
    }

    /**
     * 发布商品接口
     *
     * @param publishDto 发布信息
     * @return 成功信息
     */
    @PostMapping("/items/publish")
    public R<String> publishItem(@RequestBody ItemPublishDto publishDto) throws BizException {
        return R.success(marketService.publishItem(publishDto));
    }

    /**
     * 创建订单接口
     *
     * @param orderDto 订单信息
     * @return 订单详情
     */
    @PostMapping("/orders/create")
    public R<OrderVo> createOrder(@RequestBody OrderCreateDto orderDto) throws BizException {
        return R.success(marketService.createOrder(orderDto));
    }

    /**
     * 点赞/收藏接口
     *
     * @param id 商品 ID
     * @return 操作结果
     */
    @PostMapping("/items/like/{id}")
    public R<Object> likeItem(@PathVariable("id") String id) throws BizException {
        return R.success(marketService.likeItem(id));
    }

    /**
     * 获取评论列表接口
     *
     * @param itemId 商品 ID
     * @param queryDto 查询参数 (page, size)
     * @return 评论列表
     */
    @PostMapping("/items/comments/list/{itemId}")
    public PageResponse<MarketComment> listComments(@PathVariable("itemId") String itemId, @RequestBody CommentQueryDto queryDto) {
        return marketCommentService.listComments(itemId, queryDto.getPage(), queryDto.getSize());
    }

    /**
     * 发布评论接口
     *
     * @param addDto 评论信息
     * @return 评论 ID
     */
    @PostMapping("/items/comments/add")
    public R<String> addComment(@RequestBody CommentAddDto addDto) {
        MarketComment comment = new MarketComment();
        comment.setItemId(addDto.getItemId());
        comment.setContent(addDto.getContent());
        comment.setParentId(addDto.getParentId());
        comment.setReplyToUserId(addDto.getReplyToUserId());
        return R.success(marketCommentService.addComment(comment));
    }
}
