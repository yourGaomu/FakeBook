package com.zhangzc.booknotebiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.booknotebiz.Pojo.Domain.TNoteContent;
import com.zhangzc.booknotebiz.Service.TNoteContentService;
import com.zhangzc.booknotebiz.Mapper.TNoteContentMapper;
import org.springframework.stereotype.Service;

/**
* @author 吃饭
* @description 针对表【t_note_content(存储笔记内容的表)】的数据库操作Service实现
* @createDate 2025-08-17 00:11:26
*/
@Service
public class TNoteContentServiceImpl extends ServiceImpl<TNoteContentMapper, TNoteContent>
    implements TNoteContentService{

}




