package com.zhangzc.bookmarketbiz.Service;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookmarketapi.Dto.ItemPublishDto;
import com.zhangzc.bookmarketapi.Dto.ItemQueryDto;
import com.zhangzc.bookmarketapi.Dto.OrderCreateDto;
import com.zhangzc.bookmarketapi.Vo.MarketItemVo;
import com.zhangzc.bookmarketapi.Vo.OrderVo;

public interface MarketService {
    PageResponse<MarketItemVo> listItems(ItemQueryDto queryDto);
    MarketItemVo getItemDetail(String id) throws BizException;
    String publishItem(ItemPublishDto publishDto) throws BizException;
    OrderVo createOrder(OrderCreateDto orderDto) throws BizException;
    Object likeItem(String id) throws BizException;
}
