package com.zhangzc.bookdistributedbiz.core;


import com.zhangzc.bookdistributedbiz.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
