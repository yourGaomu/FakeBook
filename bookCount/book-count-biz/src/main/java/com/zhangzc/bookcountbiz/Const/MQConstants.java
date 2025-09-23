package com.zhangzc.bookcountbiz.Const;


public interface MQConstants {

    /**
     * 计数服务标签
     * */
    String TAG_COUNT = "Count";

    /**
     * 粉丝计数服务队列
    * */
    String TAG_COUNT_DB = "CountFansDB";

    /**
     * 笔记计数统计队列
    * */
    String TAG_COUNT_NOTE_DB = "CountNoteDB";


    /**
     * 笔记收藏和取消收藏
    * */
    String TAG_COUNT_COLLECT_UNCOLLECT = "CountCollectUnCollect";


    /**
     * 用户笔记发布
     */
    String TAG_USER_NOTE_PUBLISH = "UserNotePublish";

    /**
     * 用户收藏或者取消收藏笔记
     */
    String TOPIC_USER_COLLECT_OR_UN_COLLECT = "UserCollectUnCollectTopic";

}

