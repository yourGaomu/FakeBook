package com.zhangzc.booksearchbiz.Service.Impl;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchbiz.Mapper.SearchUserMapper;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserRspVO;
import com.zhangzc.booksearchbiz.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final SearchUserMapper searchUserMapper;

    @Override
    public PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO) {
        //获取当前的用户的搜查的内容

    }
}
