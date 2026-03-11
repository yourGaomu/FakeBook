package com.zhangzc.milvusspringbootstart.core.service.defaults;

import com.zhangzc.milvusspringbootstart.core.service.SliceService;
import com.zhangzc.milvusspringbootstart.core.service.coreService.SliceCoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@RequiredArgsConstructor
@Slf4j
public class DefaultSliceService implements SliceService {
    private final SliceCoreService sliceCoreService;

    @Override
    public List<String> slice(String text, int size) {
        //进行切片处理
        try {
            return sliceCoreService.slice(text, size);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
