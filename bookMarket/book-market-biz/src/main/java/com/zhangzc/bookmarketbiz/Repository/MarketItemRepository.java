package com.zhangzc.bookmarketbiz.Repository;

import com.zhangzc.bookmarketbiz.Domain.MarketItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketItemRepository extends MongoRepository<MarketItem, String> {
    Page<MarketItem> findAllBy(TextCriteria criteria, Pageable pageable);
    Page<MarketItem> findByCategory(String category, Pageable pageable);
}
