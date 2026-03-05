package com.zhangzc.leaf.server.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.zhangzc.leaf.core.IDGen;
import com.zhangzc.leaf.core.common.Result;
import com.zhangzc.leaf.core.common.ZeroIDGen;
import com.zhangzc.leaf.core.segment.SegmentIDGenImpl;
import com.zhangzc.leaf.core.segment.dao.IDAllocDao;
import com.zhangzc.leaf.core.segment.dao.impl.IDAllocDaoImpl;
import com.zhangzc.leaf.server.exception.InitException;
import com.zhangzc.leaf.server.properties.LeafProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class SegmentService {
    private Logger logger = LoggerFactory.getLogger(SegmentService.class);

    private IDGen idGen;
    private DruidDataSource dataSource;
    private LeafProperties leafProperties;


    public SegmentService(DruidDataSource dataSource, LeafProperties leafProperties) throws InitException {
        this.dataSource=dataSource;
        this.leafProperties=leafProperties;
        if (leafProperties.getSegmentEnable()) {
            // Config Dao
            IDAllocDao dao = new IDAllocDaoImpl(dataSource);
            // Config ID Gen
            idGen = new SegmentIDGenImpl();
            ((SegmentIDGenImpl) idGen).setDao(dao);
            if (idGen.init()) {
                logger.info("Segment Service Init Successfully");
            } else {
                throw new InitException("Segment Service Init Fail");
            }
        } else {
            idGen = new ZeroIDGen();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }

    public Result getId(String key) {
        return idGen.get(key);
    }

    public SegmentIDGenImpl getIdGen() {
        if (idGen instanceof SegmentIDGenImpl) {
            return (SegmentIDGenImpl) idGen;
        }
        return null;
    }
}
