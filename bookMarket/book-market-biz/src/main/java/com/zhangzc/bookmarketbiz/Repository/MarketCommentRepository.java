package com.zhangzc.bookmarketbiz.Repository;

import com.zhangzc.bookmarketbiz.Domain.MarketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketCommentRepository extends MongoRepository<MarketComment, String> {
    
    Page<MarketComment> findByItemId(String itemId, Pageable pageable);

    List<MarketComment> findByItemId(String itemId, Sort sort);
}
