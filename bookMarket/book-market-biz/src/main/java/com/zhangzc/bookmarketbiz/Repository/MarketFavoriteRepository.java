package com.zhangzc.bookmarketbiz.Repository;

import com.zhangzc.bookmarketbiz.Domain.MarketFavorite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketFavoriteRepository extends MongoRepository<MarketFavorite, String> {
    Optional<MarketFavorite> findByUserIdAndItemId(Long userId, String itemId);
    void deleteByUserIdAndItemId(Long userId, String itemId);
}
