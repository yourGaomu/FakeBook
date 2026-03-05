package com.zhangzc.leaf.core.segment.dao.impl;

import com.zhangzc.leaf.core.segment.dao.IDAllocDao;
import com.zhangzc.leaf.core.segment.dao.IDAllocMapper;
import com.zhangzc.leaf.core.segment.model.LeafAlloc;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;
/**
 * @description: 此处需要修改自己的mapper接口的路径，因为mybatis通过代理模式执行mapper接口中的方法
 * @author xiaojie
 * @date 2022/8/28 17:17
 * @version 1.0
 */
public class IDAllocDaoImpl implements IDAllocDao {

    SqlSessionFactory sqlSessionFactory;

    public IDAllocDaoImpl(DataSource dataSource) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(IDAllocMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    public List<LeafAlloc> getAllLeafAllocs() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList("com.zhangzc.leaf.core.segment.dao.IDAllocMapper.getAllLeafAllocs");
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public LeafAlloc updateMaxIdAndGetLeafAlloc(String tag) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession.update("com.zhangzc.leaf.core.segment.dao.IDAllocMapper.updateMaxId", tag);
            LeafAlloc result = sqlSession.selectOne("com.zhangzc.leaf.core.segment.dao.IDAllocMapper.getLeafAlloc", tag);
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession.update("com.zhangzc.leaf.core.segment.dao.IDAllocMapper.updateMaxIdByCustomStep", leafAlloc);
            LeafAlloc result = sqlSession.selectOne("com.zhangzc.leaf.core.segment.dao.IDAllocMapper.getLeafAlloc", leafAlloc.getKey());
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<String> getAllTags() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList("com.zhangzc.leaf.core.segment.dao.IDAllocMapper.getAllTags");
        } finally {
            sqlSession.close();
        }
    }
}
