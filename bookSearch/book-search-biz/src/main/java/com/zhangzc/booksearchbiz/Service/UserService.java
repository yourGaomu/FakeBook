package com.zhangzc.booksearchbiz.Service;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserReqVO;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchUserRspVO;

public interface UserService {
    PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO);
}
