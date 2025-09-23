package com.zhangzc.bookcountbiz.Service;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountapi.Pojo.Dto.Req.FindUserCountsByIdReqDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Resp.FindUserCountsByIdRspDTO;
import org.springframework.cloud.client.loadbalancer.Response;

public interface UserCountService {
    R<FindUserCountsByIdRspDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) throws BizException;
}
