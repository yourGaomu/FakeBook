package com.zhangzc.milvusspringbootstart.core.service.coreService;

import com.zhangzc.milvusspringbootstart.core.service.SliceService;
import java.util.List;

public interface SliceCoreService extends SliceService {
     List<String> slice(String text, int size);

}
