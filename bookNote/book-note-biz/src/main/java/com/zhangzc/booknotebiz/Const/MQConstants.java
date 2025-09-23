package com.zhangzc.booknotebiz.Const;


public interface MQConstants {

    /**
     * Topic 主题：删除笔记本地缓存
     */
    String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";

    String TOPIC_DELETE_NOTE_LOCAL_CACHE2 = "DeleteNoteLocalCacheTopic2";


    /**
     * 笔记收藏和取消收藏
     * */
    String TAG_COUNT_COLLECT_UNCOLLECT = "CountCollectUnCollect";

    /**
     * 笔记计数统计队列
     * */
    String TAG_COUNT_NOTE_DB = "CountNoteDB";

    /**
     * Topic: 点赞、取消点赞共用一个
     */
    String TOPIC_LIKE_OR_UNLIKE = "LikeUnlikeTopic";


    /**
     * 用户收藏或者取消收藏笔记
     */
    String TOPIC_USER_COLLECT_OR_UN_COLLECT = "UserCollectUnCollectTopic";


    String TOPIC_COLLECT_OR_UN_COLLECT = "CollectUnCollectTopic";


    /**
     * 用户笔记发布
     */
    String TAG_USER_NOTE_PUBLISH = "UserNotePublish";
}

