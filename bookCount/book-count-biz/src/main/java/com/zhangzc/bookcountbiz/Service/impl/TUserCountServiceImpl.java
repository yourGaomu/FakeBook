package com.zhangzc.bookcountbiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookcountbiz.Pojo.Domain.TUserCount;
import com.zhangzc.bookcountbiz.Service.TUserCountService;
import com.zhangzc.bookcountbiz.Mapper.TUserCountMapper;
import org.springframework.stereotype.Service;

/**
* @author 吃饭
* @description 针对表【t_user_count(用户计数表)】的数据库操作Service实现
* @createDate 2025-09-07 15:24:39
*/
@Service
public class TUserCountServiceImpl extends ServiceImpl<TUserCountMapper, TUserCount>
    implements TUserCountService{

}




