package com.zhangzc.bookuserbiz.Service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookuserbiz.Mapper.TUserMapper;
import com.zhangzc.bookuserbiz.Pojo.Domain.TUser;
import com.zhangzc.bookuserbiz.Service.TUserService;
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




