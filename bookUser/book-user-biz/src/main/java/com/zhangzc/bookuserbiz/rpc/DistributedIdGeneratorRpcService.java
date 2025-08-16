package com.zhangzc.bookuserbiz.rpc;


import com.zhangzc.bookdistributedapi.Api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


@Component
public class DistributedIdGeneratorRpcService {

    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;


    private static final String BIZ_TAG_FakeBook_ID = "leaf-segment-fakebook-id";
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";


    public String getFakeBookSegmentId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_FakeBook_ID);
    }

    public String getUserId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }
}

