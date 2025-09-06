package com.zhangzc.bookrelationbiz.Service;

import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookrelationbiz.Pojo.Vo.*;

public interface RelationService {
    R follow(FollowUserReqVO followUserReqVO);

    R unfollow(UnfollowUserReqVO unfollowUserReqVO);

    PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO);

    PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO);
}
