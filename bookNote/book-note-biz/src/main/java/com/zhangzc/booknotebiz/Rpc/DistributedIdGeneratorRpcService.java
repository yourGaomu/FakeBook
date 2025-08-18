package com.zhangzc.booknotebiz.Rpc;


import com.zhangzc.bookdistributedapi.Api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @author: 犬小哈
 * @date: 2024/4/13 23:29
 * @version: v1.0.0
 * @description: 用户服务
 **/
@Component
public class DistributedIdGeneratorRpcService {

    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    /**
     * 生成雪花算法 ID
     *
     * @return
     */
    public String getSnowflakeId() {
        return distributedIdGeneratorFeignApi.getSnowflakeId("test");
    }

}
