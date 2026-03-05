package com.zhangzc.blog.blogai.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.blog.blogai.Mapper.TSystemMessageMapper;
import com.zhangzc.blog.blogai.Pojo.domain.TSystemMessage;
import com.zhangzc.blog.blogai.Service.TSystemMessageService;
import org.springframework.stereotype.Service;

@Service
public class TSystemMessageServiceImpl extends ServiceImpl<TSystemMessageMapper, TSystemMessage> implements TSystemMessageService {
}
