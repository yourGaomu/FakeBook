package com.zhangzc.milvusspringbootstart.rerank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.milvusspringbootstart.rerank.domain.TLlmModel;

import com.zhangzc.milvusspringbootstart.rerank.mapper.TLlmModelMapper2;
import com.zhangzc.milvusspringbootstart.rerank.service.TLlmModelService;
import org.springframework.stereotype.Service;

@Service
public class TLlmModelServiceImpl extends ServiceImpl<TLlmModelMapper2, TLlmModel> implements TLlmModelService {
}
