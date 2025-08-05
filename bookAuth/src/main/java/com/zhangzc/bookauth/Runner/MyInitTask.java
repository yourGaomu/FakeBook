package com.zhangzc.bookauth.Runner;

import com.zhangzc.bookauth.Const.RedisKeyConstants;
import com.zhangzc.bookauth.Pojo.domain.TPermission;
import com.zhangzc.bookauth.Pojo.domain.TRole;
import com.zhangzc.bookauth.Pojo.domain.TRolePermissionRel;
import com.zhangzc.bookauth.Service.TPermissionService;
import com.zhangzc.bookauth.Service.TRolePermissionRelService;
import com.zhangzc.bookauth.Service.TRoleService;
import com.zhangzc.bookauth.Utils.RedisUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
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

        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        if (redisUtil.hasKey(PUSH_PERMISSION_FLAG)) {
            log.info("==> 服务启动，Redis 中已存在权限同步标记，跳过同步...");
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
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleId);
                    redisUtil.set(key, rolePermissions, 60 * 60 * 24);
                });

            }
            // 设置权限同步标记
            redisUtil.set(PUSH_PERMISSION_FLAG, "1", 60 * 60 * 24);
            log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
        } catch (Exception e) {
            log.info("==> 同步角色权限失败，请重试");
        }
    }
}

