package com.zhangzc.bookdistributedbiz.Core;

import com.zhangzc.bookdistributedbiz.Core.common.Result;
public interface IDGen {
    Result get(String key);
    boolean init();
}
