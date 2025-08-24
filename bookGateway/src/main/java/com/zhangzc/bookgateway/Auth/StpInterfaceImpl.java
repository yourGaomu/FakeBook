package com.zhangzc.bookgateway.Auth;

import cn.dev33.satoken.stp.StpInterface;
import com.zhangzc.bookgateway.Constants.RedisKeyConstants;
import com.zhangzc.bookgateway.Utils.RedisUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: zhangzc
 * @date: 2024/4/5 18:04
 * @version: v1.0.0
 * @description: 自定义权限验证接口扩展
 **/

/**
 * 自定义权限验证接口扩展
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final RedisUtil redisUtil;

    @Override
    @SneakyThrows
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 返回此 loginId 拥有的权限列表
        log.info("## 获取用户权限列表, loginId: {}", loginId);

        //从redis中获取此用户的角色列表
        List<String> roleList = getRoleList(loginId, loginType);
        if (roleList.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> permissionList = new ArrayList<>();
        for (String role : roleList) {
            System.out.println(role);
            //获取对应的角色权限key
            String rolePermissionsKey = RedisKeyConstants.buildRolePermissionsKey(role);
            Object o = redisUtil.get(rolePermissionsKey);
            if (o != null) {
                log.info("## 获取用户权限列表, loginId: {}, role: {}, permission: {}", loginId, role, o);
                //解析权限集合
                List<String> o1 = (List<String>) o;
                permissionList.addAll(o1);
            }
        }
        if (!permissionList.isEmpty()) {
            return permissionList;
        }
        return Collections.emptyList();
    }

    @Override
    @SneakyThrows
    public List<String> getRoleList(Object loginId, String loginType) {
        // 返回此 loginId 拥有的角色列表
        log.info("## 获取用户角色列表, loginId: {}", loginId);
        // 从 redis 获取角色列表
        String s = RedisKeyConstants.buildUserRoleKey(Long.valueOf(loginId.toString()));
        System.out.println(s);
        Object o = redisUtil.get(s);
        if (o != null) {
            return JsonUtils.parseObject(o.toString(), List.class);
        }
        return Collections.emptyList();
    }

}
