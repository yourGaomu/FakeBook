package com.zhangzc.bookrelationbiz.Service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.PageResponse;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookrelationbiz.Const.MQConstants;
import com.zhangzc.bookrelationbiz.Const.RedisKeyConstants;
import com.zhangzc.bookrelationbiz.Enum.LuaResultEnum;
import com.zhangzc.bookrelationbiz.Enum.ResponseCodeEnum;
import com.zhangzc.bookrelationbiz.Pojo.Domain.TFans;
import com.zhangzc.bookrelationbiz.Pojo.Domain.TFollowing;
import com.zhangzc.bookrelationbiz.Pojo.Dto.FollowUserMqDTO;
import com.zhangzc.bookrelationbiz.Pojo.Dto.UnfollowUserMqDTO;
import com.zhangzc.bookrelationbiz.Pojo.Vo.*;
import com.zhangzc.bookrelationbiz.Rpc.UserRpcService;
import com.zhangzc.bookrelationbiz.Service.RelationService;
import com.zhangzc.bookrelationbiz.Service.TFansService;
import com.zhangzc.bookrelationbiz.Service.TFollowingService;
import com.zhangzc.bookrelationbiz.Utils.DateUtils;
import com.zhangzc.bookrelationbiz.Utils.RabbitMqUtil;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class RelationServiceImpl implements RelationService {

    private final UserRpcService userRpcService;
    private final RedisTemplate redisTemplate;
    private final TFollowingService tFollowingService;
    private final TFansService tFansService;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RabbitMqUtil rabbitMqUtil;

    /**
     * 关注用户
     *
     * @param followUserReqVO
     * @return
     */
    @Override
    @SneakyThrows
    public R follow(FollowUserReqVO followUserReqVO) {
        // 关注的用户 ID
        Long followUserId = followUserReqVO.getFollowUserId();

        // 当前登录的用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        // 校验：无法关注自己
        if (Objects.equals(userId, followUserId)) {
            throw new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }

        // 校验关注的用户是否存在
        FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(followUserId);

        if (Objects.isNull(findUserByIdRspDTO)) {
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }

        // 构建当前用户关注列表的 Redis Key
        String followingRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 当前时间
        LocalDateTime now = LocalDateTime.now();
        // 当前时间转时间戳
        long timestamp = DateUtils.localDateTime2Timestamp(now);

        // 执行 Lua 脚本，拿到返回结果
        Long result = (Long) redisTemplate.execute(script, Collections.singletonList(followingRedisKey), followUserId, timestamp);

        // 校验 Lua 脚本执行结果
        checkLuaScriptResult(result);

        // ZSET 不存在
        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) {
            // 从数据库查询当前用户的关注关系记录
            List<TFollowing> followingDOS = tFollowingService.lambdaQuery().eq(TFollowing::getUserId, userId).list();

            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            // 若记录为空，直接 ZADD 对象, 并设置过期时间
            if (CollUtil.isEmpty(followingDOS)) {
                // 省略...

            } else { // 若记录不为空，则将关注关系数据全量同步到 Redis 中，并设置过期时间；
                // 构建 Lua 参数
                Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

                // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
                DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);

                // 再次调用上面的 Lua 脚本：follow_check_and_add.lua , 将最新的关注关系添加进去
                result = (Long) redisTemplate.execute(script, Collections.singletonList(followingRedisKey), followUserId, timestamp);
                checkLuaScriptResult(result);
            }
        }
        FollowUserMqDTO build = FollowUserMqDTO.builder()
                .userId(userId)
                .followUserId(followUserId)
                .createTime(now)
                .tag(MQConstants.TAG_FOLLOW)
                .build();


        threadPoolTaskExecutor.execute(() -> {
            rabbitMqUtil.send("relation.exchange", MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
                    JsonUtils.toJsonString(build));
        });


        return R.success();
    }


    /**
     * 取关用户
     *
     * @param unfollowUserReqVO
     * @return
     */
    @Override
    @SneakyThrows
    public R unfollow(UnfollowUserReqVO unfollowUserReqVO) {
        // 想要取关了用户 ID
        Long unfollowUserId = unfollowUserReqVO.getUnfollowUserId();
        // 当前登录用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        // 无法取关自己
        if (Objects.equals(userId, unfollowUserId)) {
            throw new BizException(ResponseCodeEnum.CANT_UNFOLLOW_YOUR_SELF);
        }

        // 校验关注的用户是否存在
        FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(unfollowUserId);

        if (Objects.isNull(findUserByIdRspDTO)) {
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }

        // 当前用户的关注列表 Redis Key
        String followingRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/unfollow_check_and_delete.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Long result = (Long) redisTemplate.execute(script, Collections.singletonList(followingRedisKey), unfollowUserId);

        // 校验 Lua 脚本执行结果
        // 取关的用户不在关注列表中
        if (Objects.equals(result, LuaResultEnum.NOT_FOLLOWED.getCode())) {
            throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
        }

        if (Objects.equals(result, LuaResultEnum.ZSET_NOT_EXIST.getCode())) { // ZSET 关注列表不存在
            // 从数据库查询当前用户的关注关系记录
            List<TFollowing> followingDOS = tFollowingService.lambdaQuery().eq(TFollowing::getUserId, userId).list();

            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            // 若记录为空，则表示还未关注任何人，提示还未关注对方
            if (CollUtil.isEmpty(followingDOS)) {
                throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
            } else { // 若记录不为空，则将关注关系数据全量同步到 Redis 中，并设置过期时间；
                // 构建 Lua 参数
                Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

                // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
                DefaultRedisScript<Long> script3 = new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);

                // 再次调用上面的 Lua 脚本：unfollow_check_and_delete.lua , 将取关的用户删除
                result = (Long) redisTemplate.execute(script, Collections.singletonList(followingRedisKey), unfollowUserId);
                // 再次校验结果
                if (Objects.equals(result, LuaResultEnum.NOT_FOLLOWED.getCode())) {
                    throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
                }
            }
        }

        // 发送 MQ
        // 构建消息体 DTO
        UnfollowUserMqDTO unfollowUserMqDTO = UnfollowUserMqDTO.builder()
                .userId(userId)
                .unfollowUserId(unfollowUserId)
                .createTime(LocalDateTime.now())
                .tag(MQConstants.TAG_UNFOLLOW)
                .build();


        log.info("==> 开始发送取关操作 MQ, 消息体: {}", unfollowUserMqDTO);

        // 异步发送 MQ 消息，提升接口响应速度
        threadPoolTaskExecutor.execute(() -> {
            rabbitMqUtil.send("relation.exchange", MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
                    JsonUtils.toJsonString(unfollowUserMqDTO));
        });

        return R.success();
    }

    @Override
    public PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO) {
        // 想要查询的用户 ID
        Long userId = findFollowingListReqVO.getUserId();
        // 页码
        Integer pageNo = findFollowingListReqVO.getPageNo();

        // 构建rediskey
        String followingListRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);

        // 查询目标用户关注列表 ZSet 的总大小
        long total = redisTemplate.opsForZSet().zCard(followingListRedisKey);

        // 返参
        List<FindFollowingUserRspVO> findFollowingUserRspVOS = null;

        if (total > 0) { // 缓存中有数据
            // 每页展示 10 条数据
            long limit = 10;
            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(total, limit);

            // 请求的页码超出了总页数
            if (pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            // 准备从 Redis 中查询 ZSet 分页数据
            // 每页 10 个元素，计算偏移量
            long offset = (pageNo - 1) * limit;

            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            // 注意：这里使用了 Double.POSITIVE_INFINITY 和 Double.NEGATIVE_INFINITY 作为分数范围
            // 因为关注列表最多有 1000 个元素，这样可以确保获取到所有的元素
            Set<Object> followingUserIdsSet = redisTemplate.opsForZSet()
                    .reverseRangeByScore(followingListRedisKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);

            if (CollUtil.isNotEmpty(followingUserIdsSet)) {
                // 提取所有用户 ID 到集合中
                List<Long> userIds = followingUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                // RPC: 批量查询用户信息
                List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);

                // 若不为空，DTO 转 VO
                if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
                    findFollowingUserRspVOS = findUserByIdRspDTOS.stream()
                            .map(dto -> FindFollowingUserRspVO.builder()
                                    .userId(dto.getId())
                                    .avatar(dto.getAvatar())
                                    .nickname(dto.getNickName())
                                    .introduction(dto.getIntroduction())
                                    .build())
                            .toList();
                }
            }
        } else {
            //若 Redis 中没有数据，则从数据库查询
            IPage<TFollowing> page = new Page<>(pageNo, 10);
            LambdaQueryWrapper<TFollowing> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(TFollowing::getUserId, userId)
                    .orderByDesc(TFollowing::getCreateTime);
            IPage<TFollowing> page1 = tFollowingService.page(page, queryWrapper);
            //获取记录
            //如果该分页没有结果
            if (page1.getPages() < pageNo) {
                return PageResponse.success(null, pageNo, total);
            }
            List<TFollowing> records = page1.getRecords();
            //如果该用户没有关注
            if (CollUtil.isEmpty(records)) {
                return PageResponse.success(null, pageNo, total);
            } else {
                //获取当前页码的当前谁关注了该用户的关注表
                List<Long> collect = records.stream().map(TFollowing::getFollowingUserId).toList();
                //调用远程服务获取用户信息
                List<FindUserByIdRspDTO> byIds = userRpcService.findByIds(collect);
                //经行对象转换
                List<FindFollowingUserRspVO> list = byIds.stream()
                        .map(record -> BeanUtil.copyProperties(record, FindFollowingUserRspVO.class))
                        .toList();
                findFollowingUserRspVOS = list;
                //获取有多少数据量
                Long totalUserInfo = Long.valueOf(String.valueOf(byIds.size()));
                //采用异步线程存入redis里面
                threadPoolTaskExecutor.execute(() -> {
                    syncFollowingList2Redis(userId);
                });
                //返回结果
                PageResponse.success(byIds, pageNo, totalUserInfo, 10L);
            }
        }
        return PageResponse.success(findFollowingUserRspVOS, pageNo, total);
    }

    @Override
    public PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO) {
        //获取当前的操作用户的ID
        Long userId = LoginUserContextHolder.getUserId();

        //获取需要查询用户粉丝的ID
        Long upId = findFansListReqVO.getUserId();

        //获取当前的页码
        Long pageNo = Long.valueOf(findFansListReqVO.getPageNo());

        //构建rediskey
        String userFansKey = RedisKeyConstants.buildUserFansKey(upId);

        // 查询目标用户关注列表 ZSet 的总大小
        long total = redisTemplate.opsForZSet().zCard(userFansKey);

        // 返参
        List<FindFansUserRspVO> findFansUserRspVOS = null;

        //计算当前的请求页码是否正确
        if (total > 0) {
            // 缓存中有数据
            //计算当前的页码是否超标
            // 每页展示 10 条数据
            long limit = 10;
            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(total, limit);
            // 请求的页码超出了总页数
            if (pageNo > totalPage) return PageResponse.success(null, pageNo, total);
            //进行查询
            // 准备从 Redis 中查询 ZSet 分页数据
            // 每页 10 个元素，计算偏移量
            long offset = PageResponse.getOffset(pageNo, limit);

            // 使用 ZREVRANGEBYSCORE 命令按 score 降序获取元素，同时使用 LIMIT 子句实现分页
            Set<Object> followingUserIdsSet = redisTemplate.opsForZSet()
                    .reverseRangeByScore(userFansKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, offset, limit);

            if (CollUtil.isNotEmpty(followingUserIdsSet)) {
                // 提取所有用户 ID 到集合中
                List<Long> userIds = followingUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                // RPC: 批量查询用户信息
                List<FindUserByIdRspDTO> byIds = userRpcService.findByIds(userIds);
                List<FindFansUserRspVO> list = byIds.stream().map(record -> FindFansUserRspVO.builder()
                        .userId(record.getId())
                        .avatar(record.getAvatar())
                        .nickname(record.getNickName())
                        .build()).toList();

                findFansUserRspVOS = list;
            }


        } else {
            //缓存中没有数据,从数据库中查询
            IPage<TFans> page = new Page<>(pageNo, 10);
            LambdaQueryWrapper<TFans> queryWrapper = Wrappers.lambdaQuery();

            queryWrapper.eq(TFans::getFansUserId,upId)
                    .orderByDesc(TFans::getCreateTime)
                    .last("limit 1000");

            tFansService.page(page, queryWrapper);
            //获取记录
            List<TFans> records = page.getRecords();

            if(CollUtil.isEmpty(records)){
                //数据为空
                return PageResponse.success(null, pageNo, total);
            }
            //数据存在，进行转换
            List<Long> collect = records.stream().map(TFans::getUserId).toList();
            List<FindUserByIdRspDTO> byIds = userRpcService.findByIds(collect);
            //todo 后续来修改
            List<FindFansUserRspVO> list = byIds.stream().map(record -> FindFansUserRspVO.builder()
                    .userId(record.getId())
                    .avatar(record.getAvatar())
                    .nickname(record.getNickName())
                    .fansTotal(0L)
                    .noteTotal(0L)
                    .build()).toList();
            findFansUserRspVOS = list;
            //异步线程存入数据
            threadPoolTaskExecutor.execute(() -> {
                syncFansList2Redis(upId);
            });
        }

        return  PageResponse.success(findFansUserRspVOS, pageNo, total);
    }

    private void syncFansList2Redis(Long userId) {
        // 查询粉丝列表（最多5000位用户）
        List<TFans> fansDOS = tFansService.lambdaQuery().eq(TFans::getFansUserId, userId).last("limit 5000").list();
        if (CollUtil.isNotEmpty(fansDOS)) {
            // 用户粉丝列表 Redis Key
            String fansListRedisKey = RedisKeyConstants.buildUserFansKey(userId);
            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            // 构建 Lua 参数
            Object[] luaArgs = buildFansZSetLuaArgs(fansDOS, expireSeconds);

            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(fansListRedisKey), luaArgs);
        }
    }

    private Object[] buildFansZSetLuaArgs(List<TFans> fansDOS, long expireSeconds) {
        int argsLength = fansDOS.size() * 2 + 1; // 每个粉丝关系有 2 个参数（score 和 value），再加一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (TFans fansDO : fansDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(fansDO.getCreateTime()); // 粉丝的关注时间作为 score
            luaArgs[i + 1] = fansDO.getFansUserId();          // 粉丝的用户 ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }


    /**
     * 全量同步关注列表至 Redis 中
     *
     * @param userId
     */
    private void syncFollowingList2Redis(Long userId) {
        // 查询全量关注用户列表（1000位用户）
        List<TFollowing> followingDOS = tFollowingService.lambdaQuery()
                .eq(TFollowing::getUserId, userId)
                .orderByDesc(TFollowing::getCreateTime)
                .last("limit 1000")
                .list();
        if (CollUtil.isNotEmpty(followingDOS)) {
            // 用户关注列表 Redis Key
            String followingListRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);
            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            // 构建 Lua 参数
            Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);

            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(followingListRedisKey), luaArgs);
        }
    }


    /**
     * 校验 Lua 脚本结果，根据状态码抛出对应的业务异常
     *
     * @param result
     */
    private static void checkLuaScriptResult(Long result) throws BizException {
        LuaResultEnum luaResultEnum = LuaResultEnum.valueOf(result);

        if (Objects.isNull(luaResultEnum)) throw new RuntimeException("Lua 返回结果错误");
        // 校验 Lua 脚本执行结果
        switch (luaResultEnum) {
            // 关注数已达到上限
            case FOLLOW_LIMIT -> throw new BizException(ResponseCodeEnum.FOLLOWING_COUNT_LIMIT);
            // 已经关注了该用户
            case ALREADY_FOLLOWED -> throw new BizException(ResponseCodeEnum.ALREADY_FOLLOWED);
        }
    }

    /**
     * 构建 Lua 脚本参数
     *
     * @param followingDOS
     * @param expireSeconds
     * @return
     */
    private static Object[] buildLuaArgs(List<TFollowing> followingDOS, long expireSeconds) {
        int argsLength = followingDOS.size() * 2 + 1; // 每个关注关系有 2 个参数（score 和 value），再加一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (TFollowing following : followingDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(following.getCreateTime()); // 关注时间作为 score
            luaArgs[i + 1] = following.getFollowingUserId();          // 关注的用户 ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }

}


