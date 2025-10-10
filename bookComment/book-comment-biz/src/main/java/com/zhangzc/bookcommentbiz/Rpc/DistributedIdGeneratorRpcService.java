package com.zhangzc.bookcommentbiz.Rpc;


import com.zhangzc.bookdistributedapi.Api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


@Component
public class DistributedIdGeneratorRpcService {

    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;


    private static final String BIZ_TAG_COMMENT_ID = "leaf-segment-comment-id";

    public String getCommentId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_COMMENT_ID);
    }

}

