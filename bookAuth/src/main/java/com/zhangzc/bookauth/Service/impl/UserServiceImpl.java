package com.zhangzc.bookauth.Service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.zhangzc.bookauth.Const.MQTopicConstants;
import com.zhangzc.bookauth.Const.RedisKeyConstants;
import com.zhangzc.bookauth.Const.RoleConstants;
import com.zhangzc.bookauth.Enum.DeletedEnum;
import com.zhangzc.bookauth.Enum.LoginTypeEnum;
import com.zhangzc.bookauth.Enum.ResponseCodeEnum;
import com.zhangzc.bookauth.Enum.StatusEnum;
import com.zhangzc.bookauth.Pojo.Domain.TRole;
import com.zhangzc.bookauth.Pojo.Domain.TUser;
import com.zhangzc.bookauth.Pojo.Domain.TUserRoleRel;
import com.zhangzc.bookauth.Pojo.Vo.UserLoginReqVO;

import com.zhangzc.bookauth.Pojo.Vo.UserRoleByRedis;
import com.zhangzc.bookauth.Rpc.UserRpcService;
import com.zhangzc.bookauth.Service.TRoleService;
import com.zhangzc.bookauth.Service.TUserRoleRelService;
import com.zhangzc.bookauth.Service.TUserService;
import com.zhangzc.bookauth.Service.UserService;
import com.zhangzc.bookauth.Utils.MQUtil;
import com.zhangzc.bookauth.Utils.RedisUtil;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Exceptions.ExceptionEnum;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByPhoneRspDTO;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final TUserService tuserService;
    private final TUserRoleRelService tuserRoleRelService;
    private final TRoleService tRoleService;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRpcService userRpcService;
    private final MQUtil mqUtil;


    @Override
    public R loginAndRegister(UserLoginReqVO userLoginReqVO) throws BizException {

        //用QQ邮箱来替代电话号码
        String email = userLoginReqVO.getEmail();
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
                String key = RedisKeyConstants.buildVerificationCodeKey(email);
                // 查询存储在 Redis 中该用户的登录验证码
                String sentCode = (String) redisUtil.get(key);

                // 判断用户提交的验证码，与 Redis 中的验证码是否一致
                if (!verificationCode.equals(sentCode)) {
                    throw new BizException(ExceptionEnum.VERIFICATION_CODE_ERROR);
                }

                // RPC: 调用用户服务，注册用户
                Long l = userRpcService.registerUser(email);
                System.out.println(l);
                String userIdTmp = String.valueOf(l);

                // 若调用用户服务，返回的用户 ID 为空，则提示登录失败
                if (Objects.isNull(userIdTmp)) {
                    throw new BizException(ResponseCodeEnum.LOGIN_FAIL);
                }
                userId = Long.valueOf(String.valueOf(userIdTmp));
                break;
            case PASSWORD:
                // 密码登录
                String password = userLoginReqVO.getPassword();
                // RPC: 调用用户服务，通过手机号查询用户
                FindUserByPhoneRspDTO findUserByPhoneRspDTO = userRpcService.findUserByPhone(email);

                // 判断该手机号是否注册
                if (Objects.isNull(findUserByPhoneRspDTO)) {
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }

                // 拿到密文密码
                String encodePassword = findUserByPhoneRspDTO.getPassword();

                // 匹配密码是否一致
                boolean isPasswordCorrect = passwordEncoder.matches(password, encodePassword);

                // 如果不正确，则抛出业务异常，提示用户名或者密码不正确
                if (!isPasswordCorrect) {
                    throw new BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }

                userId = findUserByPhoneRspDTO.getId();
                break;
            default:
                break;
        }

        // SaToken 登录用户，并返回 token 令牌
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        //发送异步消息进行添加用户登录
        //首先检查是否已经注册了之后存入了redis
        if (!redisUtil.hasKey(RedisKeyConstants.buildUserRoleKey(userId))) {
            UserRoleByRedis userRoleByRedis = new UserRoleByRedis();
            userRoleByRedis.setUserId(userId);
            //查询该用户的角色
            List<TUserRoleRel> userRoleRels = tuserRoleRelService.lambdaQuery()
                    .eq(TUserRoleRel::getUserId, userId)
                    .list();
            List<String> roleKeys = new ArrayList<>();
            for (TUserRoleRel userRoleRel : userRoleRels) {
                TRole byId = tRoleService.getById(userRoleRel.getRoleId());
                roleKeys.add(byId.getRoleKey());
            }
            userRoleByRedis.setRoleKeys(roleKeys);
            mqUtil.send("book.redis", MQTopicConstants.TOPIC_CREATE_USER_ROLE, JsonUtils.toJsonString(userRoleByRedis));
        }

        return R.success(tokenInfo.getTokenValue());
    }

    @Override
    public R logoutByUserId() {
        // SaToken 登出用户
        Long userId = LoginUserContextHolder.getUserId();
        StpUtil.logout(LoginUserContextHolder.getUserId());
        return R.success("退出成功");
    }

    @Override
    public R updatePassword(Map<String, String> updatePasswordReqVO) {
        // 新密码
        String newPassword = updatePasswordReqVO.get("newPassword");
        // 密码加密
        String encodePassword = passwordEncoder.encode(newPassword);

        userRpcService.updatePassword(encodePassword);

        return R.success("密码修改成功");
    }
}



