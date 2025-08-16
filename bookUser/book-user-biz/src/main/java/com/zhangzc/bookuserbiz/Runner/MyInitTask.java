package com.zhangzc.bookuserbiz.Runner;

import com.zhangzc.bookuserbiz.Const.RedisKeyConstants;
import com.zhangzc.bookuserbiz.Pojo.Domain.TPermission;
import com.zhangzc.bookuserbiz.Pojo.Domain.TRole;
import com.zhangzc.bookuserbiz.Pojo.Domain.TRolePermissionRel;
import com.zhangzc.bookuserbiz.Service.TPermissionService;
import com.zhangzc.bookuserbiz.Service.TRolePermissionRelService;
import com.zhangzc.bookuserbiz.Service.TRoleService;
import com.zhangzc.bookuserbiz.Utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyInitTask implements ApplicationRunner {

    // 权限同步标记 Key
    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";

    private final RedisUtil redisUtil;
    private final TRoleService tRoleService;
    private final TRolePermissionRelService tRolePermissionRelService;
    private final TPermissionService tPermissionService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 绿色文本（常用作成功/提示信息）
        log.info("\033[32m==> 服务启动，开始同步角色权限数据到 Redis 中...\033[0m");

        if (redisUtil.hasKey(PUSH_PERMISSION_FLAG)) {
            log.info("\033[32m==> 服务启动，Redis 中已存在权限同步标记，跳过同步...\033[0m");
            return;
        }
        try {
            //查询出所有的角色
            List<TRole> roles = tRoleService.lambdaQuery()
                    .eq(TRole::getStatus, 0)
                    .eq(TRole::getIsDeleted, 0).list();
            //如果角色不为空
            if (!roles.isEmpty()) {
                List<Long> roleIds = roles.stream().map(TRole::getId).toList();
                //维护一个角色ID到角色Key的映射
                Map<Long, String> roleIdToRoleKeyMap = roles.stream().collect(Collectors.toMap(TRole::getId, TRole::getRoleKey));

                //根据这些角色id查询出对应的权限集合
                List<TRolePermissionRel> rolePermissionReals = tRolePermissionRelService.lambdaQuery().in(TRolePermissionRel::getRoleId, roleIds).list();

                // 按角色 ID 分组, 每个角色 ID 对应多个权限 ID
                Map<Long, List<Long>> roleIdPermissionIdsMap = rolePermissionReals.stream().collect(
                        Collectors.groupingBy(TRolePermissionRel::getRoleId,
                                Collectors.mapping(TRolePermissionRel::getPermissionId, Collectors.toList()))
                );

                // 查询 APP 端所有被启用的权限
                List<TPermission> permissions = tPermissionService.lambdaQuery()
                        .eq(TPermission::getStatus, 0)
                        .eq(TPermission::getIsDeleted, 0).list();

                // 组装权限ID到权限对象的映射
                Map<Long, TPermission> permissionIdToPermissionMap = permissions.stream()
                        .collect(Collectors.toMap(TPermission::getId, permission -> permission));


                // 组装角色ID到权限对象列表的映射
                Map<Long, List<TPermission>> roleIdToPermissionsMap = new HashMap<>();

                // 为每个角色匹配对应的权限对象
                roleIdPermissionIdsMap.forEach((roleId, permissionIds) -> {
                    List<TPermission> rolePermissions = permissionIds.stream()
                            .map(permissionIdToPermissionMap::get)
                            .filter(Objects::nonNull) // 过滤掉可能不存在的权限
                            .collect(Collectors.toList());
                    roleIdToPermissionsMap.put(roleId, rolePermissions);
                });

                // 同步至 Redis 中，方便后续网关查询鉴权使用
                roleIdToPermissionsMap.forEach((roleId, rolePermissions) -> {
                    // 构建 Redis Key
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleIdToRoleKeyMap.get(roleId));
                    //获取权限标识
                    List<String> permissionKeys = rolePermissions.stream().map(TPermission::getPermissionKey).toList();
                    redisUtil.set(key, permissionKeys, 60 * 60 * 24);
                });

            }
            // 设置权限同步标记
            redisUtil.set(PUSH_PERMISSION_FLAG, "1", 60 * 60 * 24);
            log.info("\033[32m==> 服务启动，成功同步角色权限数据到 Redis 中...\033[0m");
        } catch (Exception e) {
            log.info("\033[31m==> 同步角色权限失败，请重试\033[0m");
        }
    }
}

