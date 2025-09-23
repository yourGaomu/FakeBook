package com.zhangzc.bookuserbiz.Service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.bookcountapi.Pojo.Dto.Resp.FindUserCountsByIdRspDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUsersByIdsReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByPhoneRspDTO;
import com.zhangzc.bookuserbiz.Const.MQConstants;
import com.zhangzc.bookuserbiz.Const.RedisKeyConstants;
import com.zhangzc.bookuserbiz.Const.RoleConstants;
import com.zhangzc.bookuserbiz.Enum.DeletedEnum;
import com.zhangzc.bookuserbiz.Enum.ResponseCodeEnum;
import com.zhangzc.bookuserbiz.Enum.SexEnum;
import com.zhangzc.bookuserbiz.Enum.StatusEnum;
import com.zhangzc.bookuserbiz.Pojo.Domain.TUser;
import com.zhangzc.bookuserbiz.Pojo.Domain.TUserRoleRel;
import com.zhangzc.bookuserapi.Pojo.Dto.Req.FindUserByIdReqDTO;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.bookuserbiz.Pojo.Vo.FindUserProfileReqVO;
import com.zhangzc.bookuserbiz.Pojo.Vo.FindUserProfileRspVO;
import com.zhangzc.bookuserbiz.Pojo.Vo.UpdateUserInfoReqVO;
import com.zhangzc.bookuserbiz.Service.TRoleService;
import com.zhangzc.bookuserbiz.Service.TUserRoleRelService;
import com.zhangzc.bookuserbiz.Service.TUserService;
import com.zhangzc.bookuserbiz.Service.UserService;
import com.zhangzc.bookuserbiz.Utils.ParamUtils;
import com.zhangzc.bookuserbiz.Utils.RabbitMqUtil;
import com.zhangzc.bookuserbiz.Utils.RedisUtil;
import com.zhangzc.bookuserbiz.rpc.CountRpcService;
import com.zhangzc.bookuserbiz.rpc.DistributedIdGeneratorRpcService;
import com.zhangzc.bookuserbiz.rpc.OssRpcService;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisTemplate redisTemplate;
    private final CountRpcService countRpcService;
    private final RabbitMqUtil rabbitMqUtil;


    @Override
    @SneakyThrows
    public R updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        TUser userDO = TUser.builder().build();

        //当前登录的用户id
        Long userId = LoginUserContextHolder.getUserId();

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
            tuserService.updateById(userDO);
        }

        CompletableFuture.runAsync(() -> {
            rabbitMqUtil.sendDelayMessage("delay.exchange", MQConstants.TOPIC_DELAY_USER_INFO_UPDATE, String.valueOf(userId), 3L);
        });
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
        String userRolesKey = RedisKeyConstants.buildUserRoleKey(Long.valueOf(userId));

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

    @Override
    @SneakyThrows(Exception.class)
    public R<FindUserByIdRspDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO) {
        Long id = findUserByIdReqDTO.getId();

        //从redis里面查询
        String s = (String) redisUtil.get(RedisKeyConstants.buildUserInfoKey(id));

        if (StringUtils.isNotBlank(s)) {
            FindUserByIdRspDTO findUserByIdRspDTO = JsonUtils.parseObject(s, FindUserByIdRspDTO.class);
            return R.success(findUserByIdRspDTO);
        }

        //如果没有从数据库里面查询
        TUser byId = tuserService.getById(id);

        if (Objects.isNull(byId)) {

            //存入redis中，防止缓存穿透
            redisUtil.set(RedisKeyConstants.buildUserInfoKey(id), JsonUtils.toJsonString(null));

            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        FindUserByIdRspDTO findUserByIdRspDTO = FindUserByIdRspDTO.builder()
                .id(byId.getId())
                .nickName(byId.getNickname())
                .avatar(byId.getAvatar())
                .build();

        threadPoolTaskExecutor.submit(() -> {
            try {
                // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                redisUtil.set(RedisKeyConstants.buildUserInfoKey(id), JsonUtils.toJsonString(findUserByIdRspDTO), expireSeconds);
            } catch (Exception e) {
                log.error("异步存储用户信息到Redis失败，userId:{}", id, e); // 记录异常日志
            }
        });


        return R.success(findUserByIdRspDTO);
    }


    /**
     * 批量根据用户 ID 查询用户信息
     *
     * @param findUsersByIdsReqDTO
     * @return
     */
    @Override
    public R<List<FindUserByIdRspDTO>> findByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO) {
        // 需要查询的用户 ID 集合
        List<Long> userIds = findUsersByIdsReqDTO.getIds();

        // 构建 Redis Key 集合
        List<String> redisKeys = userIds.stream()
                .map(RedisKeyConstants::buildUserInfoKey)
                .toList();

        // 先从 Redis 缓存中查, multiGet 批量查询提升性能
        List<String> redisValues = (List<String>) redisUtil.get(redisKeys);
        // 如果缓存中不为空
        if (CollUtil.isNotEmpty(redisValues)) {
            // 过滤掉为空的数据
            redisValues = redisValues.stream().filter(Objects::nonNull).toList();
        }

        // 返参
        List<FindUserByIdRspDTO> findUserByIdRspDTOS = Lists.newArrayList();

        // 将过滤后的缓存集合，转换为 DTO 返参实体类
        if (CollUtil.isNotEmpty(redisValues)) {
            findUserByIdRspDTOS = redisValues.stream()
                    .map(value -> JsonUtils.parseObject(String.valueOf(value), FindUserByIdRspDTO.class))
                    .collect(Collectors.toList());
        }

        // 如果被查询的用户信息，都在 Redis 缓存中, 则直接返回
        if (CollUtil.size(userIds) == CollUtil.size(findUserByIdRspDTOS)) {
            return R.success(findUserByIdRspDTOS);
        }

        // 还有另外两种情况：一种是缓存里没有用户信息数据，还有一种是缓存里数据不全，需要从数据库中补充
        // 筛选出缓存里没有的用户数据，去查数据库
        List<Long> userIdsNeedQuery = null;

        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            // 将 findUserInfoByIdRspDTOS 集合转 Map
            Map<Long, FindUserByIdRspDTO> map = findUserByIdRspDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdRspDTO::getId, p -> p));

            // 筛选出需要查 DB 的用户 ID
            userIdsNeedQuery = userIds.stream()
                    .filter(id -> Objects.isNull(map.get(id)))
                    .toList();
        } else { // 缓存中一条用户信息都没查到，则提交的用户 ID 集合都需要查数据库
            userIdsNeedQuery = userIds;
        }

        // 从数据库中批量查询
        List<TUser> userDOS = tuserService.lambdaQuery().in(TUser::getId, userIdsNeedQuery).list();

        List<FindUserByIdRspDTO> findUserByIdRspDTOS2 = null;

        // 若数据库查询的记录不为空


        // 异步线程将用户信息同步到 Redis 中
        List<FindUserByIdRspDTO> finalFindUserByIdRspDTOS = findUserByIdRspDTOS2;

        if (CollUtil.isNotEmpty(userDOS)) {
            // DO 转 DTO
            findUserByIdRspDTOS2 = userDOS.stream()
                    .map(userDO -> FindUserByIdRspDTO.builder()
                            .id(userDO.getId())
                            .nickName(userDO.getNickname())
                            .avatar(userDO.getAvatar())
                            .introduction(userDO.getIntroduction())
                            .build())
                    .collect(Collectors.toList());

            //异步线程将用户信息同步到 Redis 中
            threadPoolTaskExecutor.submit(() -> {
                // DTO 集合转 Map
                Map<Long, FindUserByIdRspDTO> map = finalFindUserByIdRspDTOS.stream()
                        .collect(Collectors.toMap(FindUserByIdRspDTO::getId, p -> p));

                // 执行 pipeline 操作
                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations) {
                        for (TUser userDO : userDOS) {
                            Long userId = userDO.getId();

                            // 用户信息缓存 Redis Key
                            String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);

                            // DTO 转 JSON 字符串
                            FindUserByIdRspDTO findUserInfoByIdRspDTO = map.get(userId);
                            String value = JsonUtils.toJsonString(findUserInfoByIdRspDTO);

                            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
                            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                            operations.opsForValue().set(userInfoRedisKey, value, expireSeconds, TimeUnit.SECONDS);
                        }
                        return null;
                    }
                });

            });

        }

        // 合并数据
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS2)) {
            findUserByIdRspDTOS.addAll(findUserByIdRspDTOS2);
        }

        return R.success(findUserByIdRspDTOS);
    }

    @Override
    public R<FindUserProfileRspVO> findUserProfile(FindUserProfileReqVO findUserProfileReqVO) throws BizException {
        Long userId = findUserProfileReqVO.getUserId();
        if (userId == null) {
            //up自己查看数据，保证数据的及时性
            userId = LoginUserContextHolder.getUserId();
            TUser userDO = tuserService.lambdaQuery().eq(TUser::getId, userId)
                    .eq(TUser::getIsDeleted, DeletedEnum.NO.getValue())
                    .one();

            if (Objects.isNull(userDO)) {
                throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
            }

            // 构建返参 VO
            FindUserProfileRspVO findUserProfileRspVO = FindUserProfileRspVO.builder()
                    .userId(userDO.getId())
                    .avatar(userDO.getAvatar())
                    .nickname(userDO.getNickname())
                    .xiaohashuId(userDO.getXiaohashuId())
                    .sex(userDO.getSex())
                    .introduction(userDO.getIntroduction())
                    .build();

            FindUserCountsByIdRspDTO userCountsByIdRspDTO = countRpcService.findUserCountsByIdRspDTO(userId);
            if (userCountsByIdRspDTO != null) {
                findUserProfileRspVO.setFansTotal(String.valueOf(userCountsByIdRspDTO.getFansTotal()));
                findUserProfileRspVO.setFollowingTotal(String.valueOf(userCountsByIdRspDTO.getFollowingTotal()));
                findUserProfileRspVO.setNoteTotal(String.valueOf(userCountsByIdRspDTO.getNoteTotal()));
                findUserProfileRspVO.setLikeTotal(String.valueOf(userCountsByIdRspDTO.getLikeTotal()));
                findUserProfileRspVO.setCollectTotal(String.valueOf(userCountsByIdRspDTO.getCollectTotal()));
                findUserProfileRspVO.setLikeAndCollectTotal(String.valueOf(userCountsByIdRspDTO.getCollectTotal() + userCountsByIdRspDTO.getLikeTotal()));
            }
            LocalDate birthday = TimeUtil.getLocalDate(userDO.getBirthday());
            findUserProfileRspVO.setAge(Objects.isNull(birthday) ? 0 : TimeUtil.calculateAge(birthday));
            return R.success(findUserProfileRspVO);
        }
        //从redis里面去查询
        String s = RedisKeyConstants.buildUserProfileKey(userId);
        if (redisUtil.hasKey(s)) {
            Object o = redisUtil.get(s);
            //反序列化
            FindUserProfileRspVO findUserProfileRspVO = JsonUtils.parseObject(o.toString(), FindUserProfileRspVO.class);
            return R.success(findUserProfileRspVO);
        }
        TUser userDO = tuserService.lambdaQuery().eq(TUser::getId, userId)
                .eq(TUser::getIsDeleted, DeletedEnum.NO.getValue())
                .one();

        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参 VO
        FindUserProfileRspVO findUserProfileRspVO = FindUserProfileRspVO.builder()
                .userId(userDO.getId())
                .avatar(userDO.getAvatar())
                .nickname(userDO.getNickname())
                .xiaohashuId(userDO.getXiaohashuId())
                .sex(userDO.getSex())
                .introduction(userDO.getIntroduction())
                .build();

        FindUserCountsByIdRspDTO userCountsByIdRspDTO = countRpcService.findUserCountsByIdRspDTO(userId);
        if (userCountsByIdRspDTO != null) {
            findUserProfileRspVO.setFansTotal(String.valueOf(userCountsByIdRspDTO.getFansTotal()));
            findUserProfileRspVO.setFollowingTotal(String.valueOf(userCountsByIdRspDTO.getFollowingTotal()));
            findUserProfileRspVO.setNoteTotal(String.valueOf(userCountsByIdRspDTO.getNoteTotal()));
            findUserProfileRspVO.setLikeTotal(String.valueOf(userCountsByIdRspDTO.getLikeTotal()));
            findUserProfileRspVO.setCollectTotal(String.valueOf(userCountsByIdRspDTO.getCollectTotal()));
            findUserProfileRspVO.setLikeAndCollectTotal(String.valueOf(userCountsByIdRspDTO.getCollectTotal() + userCountsByIdRspDTO.getLikeTotal()));
        }
        LocalDate birthday = TimeUtil.getLocalDate(userDO.getBirthday());
        findUserProfileRspVO.setAge(Objects.isNull(birthday) ? 0 : TimeUtil.calculateAge(birthday));
        CompletableFuture.runAsync(() -> {
            redisUtil.set(s, JsonUtils.toJsonString(findUserProfileRspVO), 60 * 60 * 24 * 30);
        });

        return R.success(findUserProfileRspVO);

    }
}

