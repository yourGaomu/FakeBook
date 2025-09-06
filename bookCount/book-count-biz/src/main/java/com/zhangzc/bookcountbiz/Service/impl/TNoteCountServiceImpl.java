package com.zhangzc.bookcountbiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookcountbiz.Domain.TNoteCount;
import com.zhangzc.bookcountbiz.Service.TNoteCountService;
import com.zhangzc.bookcountbiz.Mapper.TNoteCountMapper;
import org.springframework.stereotype.Service;

/**
* @author 吃饭
* @description 针对表【t_note_count(笔记计数表)】的数据库操作Service实现
* @createDate 2025-09-06 17:55:18
*/
@Service
public class TNoteCountServiceImpl extends ServiceImpl<TNoteCountMapper, TNoteCount>
    implements TNoteCountService{

}




