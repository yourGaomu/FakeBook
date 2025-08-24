package com.zhangzc.bookrelationbiz.Service;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookrelationbiz.Pojo.Vo.FollowUserReqVO;

public interface RelationService {
    R follow(FollowUserReqVO followUserReqVO);
}
