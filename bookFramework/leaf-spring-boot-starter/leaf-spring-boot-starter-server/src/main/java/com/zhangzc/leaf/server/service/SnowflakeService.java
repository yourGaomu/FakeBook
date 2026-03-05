package com.zhangzc.leaf.server.service;

import com.zhangzc.leaf.core.IDGen;
import com.zhangzc.leaf.core.common.Result;
import com.zhangzc.leaf.core.common.ZeroIDGen;
import com.zhangzc.leaf.core.snowflake.SnowflakeIDGenImpl;
import com.zhangzc.leaf.server.exception.InitException;
import com.zhangzc.leaf.server.properties.LeafProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeService {
    private Logger logger = LoggerFactory.getLogger(SnowflakeService.class);

    private IDGen idGen;
    private LeafProperties leafProperties;



    public SnowflakeService(LeafProperties leafProperties) throws InitException {
        this.leafProperties=leafProperties;
        if (leafProperties.getSnowflakeEnable()) {
            String zkAddress = leafProperties.getSnowflakeZkAddress();
            int port = leafProperties.getSnowflakePort();
            idGen = new SnowflakeIDGenImpl(zkAddress, port);
            if(idGen.init()) {
                logger.info("Snowflake Service Init Successfully");
            } else {
                throw new InitException("Snowflake Service Init Fail");
            }
        } else {
            idGen = new ZeroIDGen();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }

    public Result getId(String key) {
        return idGen.get(key);
    }
}
