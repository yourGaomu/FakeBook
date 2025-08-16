package com.zhangzc.bookuserbiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhangzc.bookuserbiz.Mapper.TPermissionMapper;
import com.zhangzc.bookuserbiz.Pojo.Domain.TPermission;
import com.zhangzc.bookuserbiz.Service.TPermissionService;
import org.springframework.stereotype.Service;

/**
 * @author 吃饭
 * @description 针对表【t_permission(权限表)】的数据库操作Service实现
 * @createDate 2025-07-31 15:08:15
 */
@Service
public class TPermissionServiceImpl extends ServiceImpl<TPermissionMapper, TPermission>
        implements TPermissionService {
}




