package com.zhangzc.bookuserbiz.Service.impl;

import com.google.common.base.Preconditions;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByPhoneRspDTO;
import com.zhangzc.bookuserbiz.Const.RedisKeyConstants;
import com.zhangzc.bookuserbiz.Const.RoleConstants;
import com.zhangzc.bookuserbiz.Enum.DeletedEnum;
import com.zhangzc.bookuserbiz.Enum.ResponseCodeEnum;
import com.zhangzc.bookuserbiz.Enum.SexEnum;
import com.zhangzc.bookuserbiz.Enum.StatusEnum;
import com.zhangzc.bookuserbiz.Pojo.Domain.TUser;
import com.zhangzc.bookuserbiz.Pojo.Domain.TUserRoleRel;
import com.zhangzc.bookuserbiz.Pojo.Vo.UpdateUserInfoReqVO;
import com.zhangzc.bookuserbiz.Service.TRoleService;
import com.zhangzc.bookuserbiz.Service.TUserRoleRelService;
import com.zhangzc.bookuserbiz.Service.TUserService;
import com.zhangzc.bookuserbiz.Service.UserService;
import com.zhangzc.bookuserbiz.Utils.ParamUtils;
import com.zhangzc.bookuserbiz.Utils.RedisUtil;
import com.zhangzc.bookuserbiz.rpc.DistributedIdGeneratorRpcService;
import com.zhangzc.bookuserbiz.rpc.OssRpcService;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final OssRpcService ossRpcService;
    private final DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    private final RedisUtil redisUtil;
    private final TUserRoleRelService tuserRoleRelService;
    private final TRoleService tRoleService;



    @Override
    @SneakyThrows
    public R updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        TUser userDO = TUser.builder().build();
        // 设置当前需要更新的用户 ID
        userDO.setId(LoginUserContextHolder.getUserId());
        // 标识位：是否需要更新
        boolean needUpdate = false;

        // 头像
        MultipartFile avatarFile = updateUserInfoReqVO.getAvatar();

        if (Objects.nonNull(avatarFile)) {
            String s = ossRpcService.uploadFile(avatarFile);
            if (!StringUtils.isNotBlank(s)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }
            userDO.setAvatar(s);
            needUpdate = true;
        }

        // 昵称
        String nickname = updateUserInfoReqVO.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getMeg());
            userDO.setNickname(nickname);
            needUpdate = true;
        }

        // 小哈书号
        String xiaohashuId = updateUserInfoReqVO.getXiaohashuId();
        if (StringUtils.isNotBlank(xiaohashuId)) {
            Preconditions.checkArgument(ParamUtils.checkXiaohashuId(xiaohashuId), ResponseCodeEnum.XIAOHASHU_ID_VALID_FAIL.getCode());
            userDO.setXiaohashuId(xiaohashuId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getMeg());
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)) {
            userDO.setBirthday(TimeUtil.getDateTime(birthday));
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getMeg());
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundImgFile = updateUserInfoReqVO.getBackgroundImg();
        if (Objects.nonNull(backgroundImgFile)) {
            String s = ossRpcService.uploadFile(backgroundImgFile);
            if (!StringUtils.isNotBlank(s)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }
            userDO.setBackgroundImg(s);
            needUpdate = true;
        }

        if (needUpdate) {
            // 更新用户信息
            userDO.setUpdateTime(TimeUtil.getDateTime(LocalDate.now()));
            tuserService.lambdaUpdate().eq(TUser::getId, userDO.getId()).update();
        }
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R register(Map<String, String> registerUserReqDTO) {
        String phone = registerUserReqDTO.get("phone");
        // 先判断该手机号是否已被注册
        TUser userDO1 = tuserService.lambdaQuery().eq(TUser::getPhone, phone).one();

        log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO1));

        // 若已注册，则直接返回用户 ID
        if (Objects.nonNull(userDO1)) {
            return R.success(userDO1.getId());
        }

        // 否则注册新用户
        // 获取全局自增的小哈书 ID
        //Long xiaohashuId = redisUtil.incr(RedisKeyConstants.FAKEBOOK_ID_GENERATOR_KEY, 1);
        String fakeBookSegmentId = distributedIdGeneratorRpcService.getFakeBookSegmentId();
        Long xiaohashuId = Long.valueOf(fakeBookSegmentId);

        //获取用户id
        String userId1 = distributedIdGeneratorRpcService.getUserId();

        TUser userDO = TUser.builder()
                .id(Long.valueOf(userId1))
                .phone(phone)
                .xiaohashuId(String.valueOf(xiaohashuId)) // 自动生成小红书号 ID
                .nickname("小红薯" + xiaohashuId) // 自动生成昵称, 如：小红薯10000
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

        //获取角色唯一标识
        String roleKey = tRoleService.getById(RoleConstants.COMMON_USER_ROLE_ID).getRoleKey();

        // 将该用户的角色唯一标识存入 Redis 中
        List<String> roles = new ArrayList<>(1);
        roles.add(roleKey);
        String userRolesKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(phone));

        redisUtil.set(userRolesKey, JsonUtils.toJsonString(roles));

        return R.success(userId);
    }

    @Override
    public R findByPhone(Map<String, String> findUserByPhoneReqDTO) throws BizException {
        String s = findUserByPhoneReqDTO.get("phone");

        // 根据手机号查询用户信息
        TUser userDO = tuserService.lambdaQuery().eq(TUser::getPhone, s).one();

        // 判空
        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByPhoneRspDTO findUserByPhoneRspDTO = FindUserByPhoneRspDTO.builder()
                .id(userDO.getId())
                .password(userDO.getPassword())
                .build();

        return R.success(findUserByPhoneRspDTO);
    }

    @Override
    public R updatePassword(Map<String, String> updateUserPasswordReqDTO) {
        String s = updateUserPasswordReqDTO.get("encodePassword");
        Long userId = LoginUserContextHolder.getUserId();

        tuserService.lambdaUpdate().eq(TUser::getId, userId).set(TUser::getPassword, s).update();

        return R.success("密码修改成功");
    }
}

