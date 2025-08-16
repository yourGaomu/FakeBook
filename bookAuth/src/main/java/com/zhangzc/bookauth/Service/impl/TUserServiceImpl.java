package com.zhangzc.bookauth.Service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookauth.Mapper.TUserMapper;
import com.zhangzc.bookauth.Pojo.Domain.TUser;

import com.zhangzc.bookauth.Service.TUserService;
import org.springframework.stereotype.Service;

/**
* @author 吃饭
* @description 针对表【t_user(用户表)】的数据库操作Service实现
* @createDate 2025-08-12 18:59:55
*/
@Service
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser>
    implements TUserService {

}




