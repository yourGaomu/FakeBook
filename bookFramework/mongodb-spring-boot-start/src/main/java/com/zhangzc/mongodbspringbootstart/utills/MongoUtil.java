package com.zhangzc.mongodbspringbootstart.utills;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MongoUtil {


    private final MongoTemplate mongoTemplate;

    public MongoOperations getMongoTemplate() {
        return mongoTemplate;
    }

    public <T> T insert(T objectToSave, String collectionName) {
        return mongoTemplate.insert(objectToSave, collectionName);
    }

    public <T> Collection<T> insert(Collection<? extends T> batchToSave, String collectionName) {
        return mongoTemplate.insert(batchToSave, collectionName);
    }

    public <T> T save(T objectToSave) {
        return mongoTemplate.save(objectToSave);
    }

    public <T> T save(T objectToSave, String collectionName) {
        return mongoTemplate.save(objectToSave, collectionName);
    }

    public <T> T findOne(Query query, Class<T> entityClass) {
        return mongoTemplate.findOne(query, entityClass);
    }

    public <T> T findOne(Query query, Class<T> entityClass, String collectionName) {
        return mongoTemplate.findOne(query, entityClass, collectionName);
    }

    public boolean exists(Query query, String collectionName) {
        return mongoTemplate.exists(query, collectionName);
    }

    public boolean exists(Query query, Class<?> entityClass, String collectionName) {
        return mongoTemplate.exists(query, entityClass);
    }

    public <T> List<T> find(Query query, Class<T> entityClass, String collectionName) {
        return mongoTemplate.find(query, entityClass, collectionName);
    }

    public long count(Query query, String collectionName) {
        return mongoTemplate.count(query, collectionName);
    }

    public UpdateResult updateMulti(Query query, UpdateDefinition update, Class<?> entityClass, String collectionName) {
        return mongoTemplate.updateMulti(query, update, entityClass, collectionName);
    }

    public DeleteResult remove(Object object, String collectionName) {
        return mongoTemplate.remove(object, collectionName);
    }

    /**
     * 更新第一条匹配的文档
     * @param query 查询条件
     * @param update 更新操作
     * @param entityClass 实体类
     * @param collectionName 集合名称
     * @param upsert 是否插入（如果不存在）
     * @return 更新结果
     */
    public UpdateResult updateFirst(Query query, Update update, Class<?> entityClass, String collectionName, boolean upsert) {
        if (upsert) {
            return mongoTemplate.upsert(query, update, entityClass, collectionName);
        } else {
            return mongoTemplate.updateFirst(query, update, entityClass, collectionName);
        }
    }
    
    /**
     * 更新第一条匹配的文档（简化版）
     */
    public UpdateResult updateFirst(Query query, Update update, String collectionName) {
        return mongoTemplate.updateFirst(query, update, collectionName);
    }
}
