package com.zhangzc.blog.blogai.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;
import com.zhangzc.blog.blogai.Service.TLlmModelService;
import com.zhangzc.blog.blogai.Mapper.TLlmModelMapper;
import org.springframework.stereotype.Service;

/**
* @author TraeAI
* @description 针对表【t_llm_model(LLM模型配置表)】的数据库操作Service实现
* @createDate 2026-02-17
*/
@Service
public class TLlmModelServiceImpl extends ServiceImpl<TLlmModelMapper, TLlmModel>
    implements TLlmModelService{

}
