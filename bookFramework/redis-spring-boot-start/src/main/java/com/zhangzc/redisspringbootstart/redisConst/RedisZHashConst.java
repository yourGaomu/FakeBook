package com.zhangzc.redisspringbootstart.redisConst;

public class RedisZHashConst {
    public static final String ARTICLE_MONGO_INFO = "article:mongo:info";
    public static final String MARKET_ITEM_LIKES = "market:item:likes:";

    public static String getArticleMongoInfo(String articleId) {
        return ARTICLE_MONGO_INFO + articleId;
    }

    public static String getMarketItemLikes(String itemId) {
        return MARKET_ITEM_LIKES + itemId;
    }

}
