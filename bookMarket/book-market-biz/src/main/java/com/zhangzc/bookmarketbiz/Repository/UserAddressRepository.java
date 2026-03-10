package com.zhangzc.bookmarketbiz.Repository;

import com.zhangzc.bookmarketbiz.Domain.UserAddress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends MongoRepository<UserAddress, String> {
    /**
     * 根据用户 ID 查询地址列表
     * @param userId 用户 ID
     * @return 地址列表
     */
    List<UserAddress> findByUserId(Long userId);
}
