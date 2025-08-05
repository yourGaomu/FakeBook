package com.zhangzc.bookauth.Service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.collect.Lists;
import com.zhangzc.bookauth.Const.RedisKeyConstants;
import com.zhangzc.bookauth.Const.RoleConstants;
import com.zhangzc.bookauth.Enum.DeletedEnum;
import com.zhangzc.bookauth.Enum.LoginTypeEnum;
import com.zhangzc.bookauth.Enum.StatusEnum;
import com.zhangzc.bookauth.Pojo.Vo.UserLoginReqVO;
import com.zhangzc.bookauth.Pojo.domain.TUser;
import com.zhangzc.bookauth.Pojo.domain.TUserRoleRel;
import com.zhangzc.bookauth.Service.TUserRoleRelService;
import com.zhangzc.bookauth.Service.TUserService;
import com.zhangzc.bookauth.Service.UserService;
import com.zhangzc.bookauth.Utils.RedisUtil;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Exceptions.ExceptionEnum;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final TUserService tuserService;
    private final TUserRoleRelService tuserRoleRelService;
    private final RedisUtil redisUtil;


    @Override
    public R loginAndRegister(UserLoginReqVO userLoginReqVO) throws BizException {
        //用QQ邮箱来替代电话号码
        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);

        Long userId = null;

        // 判断登录类型
        switch (loginTypeEnum) {
            case VERIFICATION_CODE: // 验证码登录
                String verificationCode = userLoginReqVO.getCode();

                // 校验入参验证码是否为空
                if (StringUtils.isBlank(verificationCode)) {
                    return R.fail(String.valueOf(500), "验证码不能为空");
                }

                // 构建验证码 Redis Key
                String key = RedisKeyConstants.buildVerificationCodeKey(phone);
                // 查询存储在 Redis 中该用户的登录验证码
                String sentCode = (String) redisUtil.get(key);

                // 判断用户提交的验证码，与 Redis 中的验证码是否一致
                if (StringUtils.equals(verificationCode, sentCode)) {
                    throw new BizException(ExceptionEnum.VERIFICATION_CODE_ERROR);
                }

                // 通过手机号查询记录
                TUser userDO = tuserService.lambdaQuery().eq(TUser::getPhone, phone).one();

                log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO));

                // 判断是否注册
                if (Objects.isNull(userDO)) {
                    // 若此用户还没有注册，系统自动注册该用户
                    userId = register(phone);

                } else {
                    // 已注册，则获取其用户 ID
                    userId = userDO.getId();
                }
                break;
            case PASSWORD: // 密码登录
                // todo

                break;
            default:
                break;
        }

        // SaToken 登录用户，并返回 token 令牌
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return R.success(tokenInfo.getTokenValue());
    }


    @Transactional(rollbackFor = Exception.class)
    protected Long register(String qq) {
        // 获取全局自增的小哈书 ID
        Long fakebookId = Long.valueOf((String) redisUtil.get(RedisKeyConstants.FAKEBOOK_ID_GENERATOR_KEY));
        //再去自增id
        redisUtil.incr(RedisKeyConstants.FAKEBOOK_ID_GENERATOR_KEY, 1);

        TUser userDO = TUser.builder()
                .phone(qq)
                .xiaohashuId(String.valueOf(fakebookId)) // 自动生成小红书号 ID
                .nickname("假书" + fakebookId) // 自动生成昵称, 如：小红薯10000
                .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                .createTime(TimeUtil.getDateTime(LocalDate.now()))
                .updateTime(TimeUtil.getDateTime(LocalDate.now()))
                .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                .build();

        // 添加入库
        tuserService.save(userDO);

        // 获取刚刚添加入库的用户 ID
        Long userId = userDO.getId();

        // 给该用户分配一个默认角色
        TUserRoleRel userRoleDO = TUserRoleRel.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(TimeUtil.getDateTime(LocalDate.now()))
                .updateTime(TimeUtil.getDateTime(LocalDate.now()))
                .isDeleted(DeletedEnum.NO.getValue())
                .build();

        tuserRoleRelService.save(userRoleDO);

        // 将该用户的角色 ID 存入 Redis 中
        List<Long> roles = Lists.newArrayList();
        roles.add(RoleConstants.COMMON_USER_ROLE_ID);
        String userRolesKey = RedisKeyConstants.buildUserRoleKey(qq);
        redisUtil.set(userRolesKey, JsonUtils.toJsonString(roles));

        return userId;
    }
}



