package com.zhangzc.bookrelationbiz.Service;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookrelationbiz.Pojo.Vo.FindFollowingListReqVO;
import com.zhangzc.bookrelationbiz.Pojo.Vo.FindFollowingUserRspVO;
import com.zhangzc.bookrelationbiz.Pojo.Vo.FollowUserReqVO;
import com.zhangzc.bookrelationbiz.Pojo.Vo.UnfollowUserReqVO;

public interface RelationService {
    R follow(FollowUserReqVO followUserReqVO);

    R unfollow(UnfollowUserReqVO unfollowUserReqVO);

    PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO);
}
