package com.zhangzc.bookmarketbiz.Repository;

import com.zhangzc.bookmarketbiz.Domain.MarketOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketOrderRepository extends MongoRepository<MarketOrder, String> {
}
