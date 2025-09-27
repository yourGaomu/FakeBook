package com.zhangzc.booksearchbiz.Service.Impl;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchbiz.Mapper.Es.SearchUserMapper;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserRspVO;
import com.zhangzc.booksearchbiz.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dromara.easyes.core.biz.EsPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final SearchUserMapper searchUserMapper;

    @Override
    public PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO) {
        //获取当前的用户的搜查的内容
        String keyword = searchUserReqVO.getKeyword();
        Integer pageNo = searchUserReqVO.getPageNo();
        if (StringUtils.isEmpty(keyword)) {
            return new PageResponse<SearchUserRspVO>();
        }
        if (pageNo < 1) {
            pageNo = 1;
        }
        //查询开始
        LambdaEsQueryWrapper<SearchUserRspVO> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(SearchUserRspVO::getNickname, keyword)
                .orderByDesc(SearchUserRspVO::getFansTotal);
        EsPageInfo<SearchUserRspVO> searchUserRspVOEsPageInfo = searchUserMapper.pageQuery(wrapper, pageNo, 10);
        Long total = searchUserRspVOEsPageInfo.getTotal();
        if (total == 0) {
            return new PageResponse<>();
        }
        List<SearchUserRspVO> list = searchUserRspVOEsPageInfo.getList();
        return PageResponse.success(list, pageNo, total);

    }
}
