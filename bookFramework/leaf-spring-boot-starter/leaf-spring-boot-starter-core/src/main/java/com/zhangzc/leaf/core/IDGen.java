package com.zhangzc.leaf.core;

import com.zhangzc.leaf.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
