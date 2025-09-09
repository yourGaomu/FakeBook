package com.zhangzc.booknotebiz.Const;


public interface MQConstants {

    /**
     * Topic 主题：删除笔记本地缓存
     */
    String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";

    String TOPIC_DELETE_NOTE_LOCAL_CACHE2 = "DeleteNoteLocalCacheTopic2";



    /**
     * Topic: 点赞、取消点赞共用一个
     */
    String TOPIC_LIKE_OR_UNLIKE = "LikeUnlikeTopic";
}

