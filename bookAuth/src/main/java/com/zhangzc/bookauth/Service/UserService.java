package com.zhangzc.bookauth.Service;

import com.zhangzc.bookauth.Pojo.Vo.UserLoginReqVO;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;

public interface UserService {
   public R loginAndRegister(UserLoginReqVO userLoginReqVO) throws BizException;
}
