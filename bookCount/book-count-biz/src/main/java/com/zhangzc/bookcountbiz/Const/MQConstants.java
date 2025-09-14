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
}

