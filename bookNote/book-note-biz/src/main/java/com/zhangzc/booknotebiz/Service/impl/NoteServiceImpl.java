package com.zhangzc.booknotebiz.Service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.io.grpc.internal.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.rabbitmq.client.LongString;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdRspDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Resp.FindUserCountsByIdRspDTO;
import com.zhangzc.booknotebiz.Const.MQConstants;
import com.zhangzc.booknotebiz.Const.RedisKeyConstants;
import com.zhangzc.booknotebiz.Enum.NoteStatusEnum;
import com.zhangzc.booknotebiz.Enum.NoteTypeEnum;
import com.zhangzc.booknotebiz.Enum.NoteVisibleEnum;
import com.zhangzc.booknotebiz.Enum.ResponseCodeEnum;
import com.zhangzc.booknotebiz.Pojo.Domain.*;
import com.zhangzc.booknotebiz.Pojo.Dto.CollectUnCollectNoteMqDTO;
import com.zhangzc.booknotebiz.Pojo.Dto.LikeUnlikeNoteMqDTO;
import com.zhangzc.booknotebiz.Pojo.Dto.PublishNoteMqDTO;
import com.zhangzc.booknotebiz.Pojo.Vo.*;
import com.zhangzc.booknotebiz.Rpc.CountRpcService;
import com.zhangzc.booknotebiz.Rpc.DistributedIdGeneratorRpcService;
import com.zhangzc.booknotebiz.Rpc.KeyValueRpcService;
import com.zhangzc.booknotebiz.Rpc.UserRpcService;
import com.zhangzc.booknotebiz.Service.*;
import com.zhangzc.booknotebiz.Utils.DateUtils;
import com.zhangzc.booknotebiz.Utils.RabbitMqUtil;
import com.zhangzc.booknotebiz.Utils.RedisHashExample;
import com.zhangzc.booknotebiz.Utils.RedisUtil;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;


import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.crypto.Mac;

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    private final CountRpcService countRpcService;
    private final UserRpcService userRpcService;
    private final TTopicService tTopicService;
    private final TNoteService tNoteService;
    private final TNoteContentService tNoteContentService;
    private final KeyValueRpcService keyValueRpcService;
    private final RedisUtil redisUtil;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RabbitMqUtil rabbitMqUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TNoteCountService tNoteCountService;
    private final TNoteLikeService tNoteLikeService;
    private final TNoteCollectionService tNoteCollectService;
    private final RedisHashExample redisHashExample;
    private final TChannelService tChannelService;
    private final TChannelTopicRelService tChannelTopicRelService;

    // 手动编写构造函数，为线程池参数添加 @Qualifier
    public NoteServiceImpl(
            CountRpcService countRpcService,
            TChannelTopicRelService tChannelTopicRelService,
            TChannelService tChannelService,
            RedisHashExample redisHashExample,
            DistributedIdGeneratorRpcService distributedIdGeneratorRpcService,
            UserRpcService userRpcService,
            TTopicService tTopicService,
            TNoteService tNoteService,
            TNoteContentService tNoteContentService,
            KeyValueRpcService keyValueRpcService,
            RedisUtil redisUtil,
            @Qualifier("taskExecutor") ThreadPoolTaskExecutor threadPoolTaskExecutor,
            RabbitMqUtil rabbitMqUtil,
            RedisTemplate<String, Object> redisTemplate,
            TNoteCountService tNoteCountService,
            TNoteLikeService tNoteLikeService,
            TNoteCollectionService tNoteCollectionService
    ) {
        this.countRpcService = countRpcService;
        this.tChannelTopicRelService = tChannelTopicRelService;
        this.tChannelService = tChannelService;
        this.redisHashExample = redisHashExample;
        this.distributedIdGeneratorRpcService = distributedIdGeneratorRpcService;
        this.userRpcService = userRpcService;
        this.tTopicService = tTopicService;
        this.tNoteService = tNoteService;
        this.tNoteContentService = tNoteContentService;
        this.keyValueRpcService = keyValueRpcService;
        this.redisUtil = redisUtil;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.rabbitMqUtil = rabbitMqUtil;
        this.redisTemplate = redisTemplate;
        this.tNoteCountService = tNoteCountService;
        this.tNoteLikeService = tNoteLikeService;
        this.tNoteCollectService = tNoteCollectionService;
    }


    /**
     * 笔记发布
     *
     * @param publishNoteReqVO
     * @return
     */
    @Override
    @SneakyThrows
    public R publishNote(PublishNoteReqVO publishNoteReqVO) {
        // 笔记类型
        Integer type = publishNoteReqVO.getType();

        // 获取对应类型的枚举
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        // 若非图文、视频，抛出业务业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        // 笔记内容是否为空，默认值为 true，即空
        Boolean isContentEmpty = true;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT:
                // 图文笔记
                List<String> imgUriList = publishNoteReqVO.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");
                // 将图片链接拼接，以逗号分隔
                imgUris = StringUtils.join(imgUriList, ",");

                break;
            case VIDEO:
                // 视频笔记
                videoUri = publishNoteReqVO.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }

        // RPC: 调用分布式 ID 生成服务，生成笔记 ID
        String snowflakeIdId = distributedIdGeneratorRpcService.getSnowflakeId();
        // 笔记内容 UUID
        String contentUuid = null;

        // 笔记内容
        String content = publishNoteReqVO.getContent();

        // 若用户填写了笔记内容
        if (StringUtils.isNotBlank(content)) {
            // 内容是否为空，置为 false，即不为空
            isContentEmpty = false;
            // 生成笔记内容 UUID
            contentUuid = UUID.randomUUID().toString();
            // RPC: 调用 KV 键值服务，存储短文本
            boolean isSavedSuccess = keyValueRpcService.saveNoteContent(contentUuid, content);

            // 若存储失败，抛出业务异常，提示用户发布笔记失败
            if (!isSavedSuccess) {
                throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
            }
        }

        // 话题
        Long topicId = publishNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            // 获取话题名称
            topicName = tTopicService.getById(topicId).getName();
        }

        // 发布者用户 ID
        Long creatorId = LoginUserContextHolder.getUserId();

        // 构建笔记 DO 对象
        TNote noteDO = TNote.builder()
                .id(Long.valueOf(snowflakeIdId))
                .isContentEmpty(isContentEmpty)
                .creatorId(creatorId)
                .imgUris(imgUris)
                .title(publishNoteReqVO.getTitle())
                .topicId(publishNoteReqVO.getTopicId())
                .topicName(topicName)
                .type(type)
                .visible(NoteVisibleEnum.PUBLIC.getCode())
                .createTime(TimeUtil.getDateTime(LocalDate.now()))
                .updateTime(TimeUtil.getDateTime(LocalDate.now()))
                .status(NoteStatusEnum.NORMAL.getCode())
                .isTop(Boolean.FALSE)
                .videoUri(videoUri)
                .contentUuid(contentUuid)
                .build();

        try {
            // 笔记入库存储
            tNoteService.save(noteDO);
            //发送MQ笔记存入用户
            PublishNoteMqDTO publishNoteMqDTO = PublishNoteMqDTO.builder()
                    .userId(creatorId)
                    .build();

            rabbitMqUtil.send("count.exchange", MQConstants.TAG_USER_NOTE_PUBLISH, JsonUtils.toJsonString(publishNoteMqDTO));
        } catch (Exception e) {
            log.error("==> 笔记存储失败", e);
            // RPC: 笔记保存失败，则删除笔记内容
            if (StringUtils.isNotBlank(contentUuid)) {
                keyValueRpcService.deleteNoteContent(contentUuid);
            }
        }

        return R.success();
    }

    @Override
    @SneakyThrows
    public R<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO) {
        //获取笔记id
        Long id = findNoteDetailReqVO.getId();
        //获取当前的登录用户
        Long userId = LoginUserContextHolder.getUserId();
        //先从redis查询笔记
        String key = RedisKeyConstants.buildNoteDetailKey(id);
        FindNoteDetailRspVO o = (FindNoteDetailRspVO) redisUtil.get(key);
        if (o != null) {
            // 可见性校验
            Integer visible = o.getVisible();
            checkNoteVisible(visible, userId, o.getCreatorId());
            return R.success(o);
        }
        //如果redis里面不存在则查询开始
        TNote noteDO = tNoteService.lambdaQuery().eq(TNote::getId, id).one();
        if (Objects.isNull(noteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 可见性校验
        Integer visible = noteDO.getVisible();
        checkNoteVisible(visible, userId, noteDO.getCreatorId());
        //获取笔记内容的uuid
        String contentUuid = noteDO.getContentUuid();
        //使用异步查询笔记内容
        CompletableFuture<TNoteContent> contentCompletableFuture = CompletableFuture.completedFuture(null);
        //如果笔记内容不为空
        if (Objects.equals(noteDO.getIsContentEmpty(), Boolean.FALSE)) {
            contentCompletableFuture = CompletableFuture.supplyAsync(() -> tNoteContentService.lambdaQuery().eq(TNoteContent::getId, contentUuid).one(), threadPoolTaskExecutor);
        }

        //使用异步查询笔记主人的信息
        Long creatorId = noteDO.getCreatorId();
        CompletableFuture<FindUserByIdRspDTO> completableFuture = CompletableFuture.supplyAsync(() -> userRpcService.findById(creatorId), threadPoolTaskExecutor);
        // 笔记类型
        Integer noteType = noteDO.getType();
        // 图文笔记图片链接(字符串)
        String imgUrisStr = noteDO.getImgUris();
        // 图文笔记图片链接(集合)
        List<String> imgUris;
        // 如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
        if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode())
                && StringUtils.isNotBlank(imgUrisStr)) {
            imgUris = List.of(imgUrisStr.split(","));
        } else {
            imgUris = null;
        }

        //开始收集任务序列
        CompletableFuture<TNoteContent> finalContentCompletableFuture = contentCompletableFuture;
        CompletableFuture<FindNoteDetailRspVO> findNoteDetailReqVOCompletableFuture = CompletableFuture.allOf(finalContentCompletableFuture, completableFuture).thenApply(result -> {
            //获取笔记内容
            TNoteContent one1 = finalContentCompletableFuture.join();
            //获取笔记主人的信息
            FindUserByIdRspDTO creator = completableFuture.join();
            // 构建返参 VO 实体类
            return FindNoteDetailRspVO.builder()
                    .id(noteDO.getId())
                    .type(noteDO.getType())
                    .title(noteDO.getTitle())
                    .content(one1 != null ? one1.getContent() : null)  // 防止one1为null时的空指针
                    .imgUris(imgUris)
                    .topicId(noteDO.getTopicId())
                    .topicName(noteDO.getTopicName())
                    .creatorId(noteDO.getCreatorId())
                    .creatorName(creator.getNickName())
                    .avatar(creator.getAvatar())
                    .videoUri(noteDO.getVideoUri())
                    .updateTime(TimeUtil.getLocalDateTime(noteDO.getUpdateTime()))
                    .visible(noteDO.getVisible())
                    .build();
        });

        //获取结果
        FindNoteDetailRspVO findNoteDetailRspVO = findNoteDetailReqVOCompletableFuture.join();

        //开启线程去存入redis
        threadPoolTaskExecutor.submit(() -> {
            try {
                //获取随机的存储时间
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                redisUtil.set(key, Objects.isNull(findNoteDetailRspVO) ? null : findNoteDetailRspVO, expireSeconds);
            } catch (Exception e) {
                log.error("异步存储Redis失败，key:{}", key, e);
            }
        });
        return R.success(findNoteDetailRspVO);
    }

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public R updateNote(UpdateNoteReqVO updateNoteReqVO) {


        //获取笔记的类型
        Integer type = updateNoteReqVO.getType();
        //如果不是图文或者视频
        if (!NoteTypeEnum.isValid(type)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }
        //获取对应的枚举类
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        String imgUris = null;
        String videoUri = null;
        //根据笔记的类型去判断
        switch (noteTypeEnum) {
            case IMAGE_TEXT: // 图文笔记
                List<String> imgUriList = updateNoteReqVO.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");

                imgUris = StringUtils.join(imgUriList, ",");
                break;
            case VIDEO: // 视频笔记
                videoUri = updateNoteReqVO.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }


        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();
        TNote selectNoteDO = tNoteService.getById(updateNoteReqVO.getId());

        // 笔记不存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许更新笔记
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }


        //获取笔记的话题
        Long topicId = updateNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            TTopic byId = tTopicService.getById(topicId);
            if (Objects.isNull(byId)) {
                throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
            }
            topicName = byId.getName(); // 复用查询结果
        }

        //获取笔记的id
        Long id = updateNoteReqVO.getId();
        //获取笔记的内容
        String content = updateNoteReqVO.getContent();
        //获取笔记是否为空
        Boolean isContentEmpty = StringUtils.isBlank(content);

        //查询笔记内容DO
        TNoteContent tNoteContent = tNoteContentService.getById(id);

        //开始更新笔记
        try {

            //更新笔记内容
            if (Objects.equals(isContentEmpty, Boolean.FALSE)) {
                //笔记不为空
                if (tNoteContent == null) {
                    //笔记内容存储为空,第一次插入
                    tNoteContent = new TNoteContent();
                    tNoteContent.setId(UUID.randomUUID().toString());
                    tNoteContent.setContent(content);
                    tNoteContentService.save(tNoteContent);
                } else {
                    tNoteContent.setContent(content);
                    tNoteContentService.updateById(tNoteContent);
                }
            } else {
                //笔记为空
                if (tNoteContent != null) {
                    //笔记内容存储不为空
                    tNoteContentService.removeById(tNoteContent.getId());
                    tNoteContent.setId(null);
                }
            }
            //延时双删
            threadPoolTaskExecutor.execute(() -> {
                try {
                    //获取key
                    String key = RedisKeyConstants.buildNoteDetailKey(id);
                    redisUtil.del(key);
                } catch (Exception e) {
                    log.error("异步删除笔记Redis缓存失败，noteId:{}", id, e); // 仅记录日志，不抛出
                }
            });

            //更新笔记的基本信息
            tNoteService.lambdaUpdate()
                    .eq(TNote::getId, id)
                    .set(TNote::getTitle, updateNoteReqVO.getTitle())
                    .set(TNote::getIsContentEmpty, isContentEmpty)
                    .set(TNote::getImgUris, imgUris)
                    .set(TNote::getVideoUri, videoUri)
                    .set(TNote::getTopicId, topicId)
                    .set(TNote::getTopicName, topicName)
                    .set(TNote::getUpdateTime, TimeUtil.getDateTime(LocalDate.now()))
                    .set(TNote::getContentUuid, tNoteContent == null ? null : tNoteContent.getId())
                    .set(TNote::getType, type).update();
            //处理redis里面的存储内容
            threadPoolTaskExecutor.execute(() -> {
                try {
                    //获取key
                    String key = RedisKeyConstants.buildNoteDetailKey(id);
                    //延时删除
                    rabbitMqUtil.sendDelayMessage("delay.exchange", MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, key, 3L);
                } catch (Exception e) {
                    log.error("异步删除笔记Redis缓存失败，noteId:{}", id, e); // 仅记录日志，不抛出
                }
            });
        } catch (Exception e) {
            log.error("==> 笔记更新失败", e);
            throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
        }
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public R deleteNote(DeleteNoteReqVO deleteNoteReqVO) {
        // 笔记 ID
        Long noteId = deleteNoteReqVO.getId();

        //判断是否是本人操作
        TNote byId = tNoteService.getById(noteId);
        if (!Objects.equals(byId.getCreatorId(), LoginUserContextHolder.getUserId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 逻辑删除
        TNote noteDO = TNote.builder()
                .id(noteId)
                .status(NoteStatusEnum.DELETED.getCode())
                .updateTime(TimeUtil.getDateTime(LocalDate.now()))
                .build();

        boolean b = tNoteService.updateById(noteDO);

        // 若影响的行数为 0，则表示该笔记不存在
        if (!b) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        //todo如果删除了本地缓存，对应的用户点赞的记录也应该删除
        //todo 这里还要重新写一下，记得去删除用户的发布数
        rabbitMqUtil.sendDelayMessage("delay.exchange", MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, String.valueOf(noteId), 3L);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        return R.success();
    }

    @Override
    @SneakyThrows
    public R visibleOnlyMe(UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO) {
        // 获取笔记 ID
        Long noteId = updateNoteVisibleOnlyMeReqVO.getId();

        //获取当前操作用户id
        Long userId = LoginUserContextHolder.getUserId();

        //判断是否是本人操作
        TNote byId = tNoteService.getById(noteId);
        if (!Objects.equals(byId.getCreatorId(), userId)) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        //更新笔记可见性
        boolean update = tNoteService.lambdaUpdate()
                .eq(TNote::getId, noteId)
                .eq(TNote::getCreatorId, userId)
                .set(TNote::getVisible, NoteVisibleEnum.PRIVATE.getCode())
                .set(TNote::getUpdateTime, TimeUtil.getDateTime(LocalDate.now()))
                .update();

        if (!update) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_VISIBLE_ONLY_ME);
        }

        return R.success();
    }

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public R topNote(TopNoteReqVO topNoteReqVO) {
        // 笔记 ID
        Long noteId = topNoteReqVO.getId();
        // 是否置顶
        Boolean isTop = topNoteReqVO.getIsTop();

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();

        boolean update = tNoteService.lambdaUpdate()
                .eq(TNote::getId, noteId)
                .eq(TNote::getCreatorId, currUserId)
                .set(TNote::getIsTop, isTop)
                .set(TNote::getUpdateTime, TimeUtil.getDateTime(LocalDate.now()))
                .update();

        if (!update) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisUtil.del(noteDetailRedisKey);

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rabbitMqUtil.sendDelayMessage("delay.exchange", MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteDetailRedisKey, 3L);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        return R.success();
    }

    @Override
    @SneakyThrows
    public R likeNote(LikeNoteReqVO likeNoteReqVO) {
        //当前的笔记ID
        Long noteId = likeNoteReqVO.getId();
        if (noteId == null) {
            throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
        }
        //先判断是否存在
        handleExistNote(likeNoteReqVO);

        //判断了之后再去判断是否点赞过
        //当前登录者的id
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            //todo
            userId = 103L;
        }
        // 布隆过滤器 Key
        String bloomUserNoteLikeListKey = RedisKeyConstants.buildBloomUserNoteLikeListKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_like_check.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Integer result = Integer.valueOf(String.valueOf(redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), noteId)));
        //根据返回结果去判断
        switch (result) {
            case 1:
                //笔记可能被点赞过,需要进行更多的检验
                String userNoteLikeZSetKey = RedisKeyConstants.buildUserNoteLikeZSetKey(userId);
                //进行检redis中是否存在
                if (!redisUtil.hasKey(userNoteLikeZSetKey)) {
                    InitUserNoteLikeZSet(userNoteLikeZSetKey, userId, 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24));
                }
                Double score = redisTemplate.opsForZSet().score(userNoteLikeZSetKey, noteId);
                if (Objects.nonNull(score)) {
                    throw new BizException(ResponseCodeEnum.NOTE_LIKE_REPEAT);
                } else {
                    TNoteLike one = tNoteLikeService.lambdaQuery().eq(TNoteLike::getNoteId, noteId)
                            .eq(TNoteLike::getUserId, userId).one();
                    if (one != null && one.getStatus() == 1) {
                        //用户已经点赞
                        throw new BizException(ResponseCodeEnum.NOTE_LIKE_REPEAT);
                    }
                }
                break;
            case -1:
                //布隆过滤器不存在
                // 从数据库中校验笔记是否被点赞，并异步初始化布隆过滤器，设置过期时间
                // 保底1天+随机秒数
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                TNoteLike one = tNoteLikeService.lambdaQuery()
                        .eq(TNoteLike::getNoteId, noteId)
                        .eq(TNoteLike::getUserId, userId)
                        .eq(TNoteLike::getStatus, 1).one();
                InitBloomFilter(bloomUserNoteLikeListKey, userId, expireSeconds);
                if (!Objects.isNull(one)) {
                    //说明用户已经点赞过这个笔记
                    throw new BizException(ResponseCodeEnum.NOTE_LIKE_REPEAT);
                } else {
                    //用户没有点赞的记录
                    // Lua 脚本路径
                    script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_note_like_and_expire.lua")));
                    // 返回值类型
                    script.setResultType(Long.class);
                    redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), noteId, expireSeconds);
                }
                break;
        }
        //数据写入Zset里面
        String userNoteLikeZSetKey = RedisKeyConstants.buildUserNoteLikeZSetKey(userId);
        // 3. 更新用户 ZSET 点赞列表
        LocalDateTime now = LocalDateTime.now();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/note_like_check_and_update_zset.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        result = Integer.valueOf(String.valueOf(redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), noteId, DateUtils.localDateTime2Timestamp(now))));

        // 若 ZSet 列表不存在，需要重新初始化
        if (Objects.equals(result, -1)) {
            //Zset不存在
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            //去加载之前的数据
            InitUserNoteLikeZSet(userNoteLikeZSetKey, userId, expireSeconds);
            //判断Zset是否创建成功，如果用户没有任何点赞记录则不会建立
            if (redisUtil.hasKey(userNoteLikeZSetKey)) {
                //用户第一次点赞
                List<Object> luaArgs = Lists.newArrayList();
                luaArgs.add(DateUtils.localDateTime2Timestamp(LocalDateTime.now())); // score ：点赞时间戳
                luaArgs.add(noteId); // 当前点赞的笔记 ID
                luaArgs.add(expireSeconds); // 随机过期时间
                DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                // Lua 脚本路径
                script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_like_zset_and_expire.lua")));
                // 返回值类型
                script2.setResultType(Long.class);
                redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs.toArray());

            } else {
                //加入当前需要加入bloom笔记点赞
                redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));
            }

        }

        //异步加入数据库
        threadPoolTaskExecutor.execute(() -> {
            LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = LikeUnlikeNoteMqDTO.builder()
                    .userId(103L)
                    .noteId(noteId)
                    .type(1)
                    .createTime(now)
                    .build();
            rabbitMqUtil.send("likeOrUnlike.exchange", MQConstants.TOPIC_LIKE_OR_UNLIKE, JsonUtils.toJsonString(likeUnlikeNoteMqDTO));
        });

        return R.success();
    }

    @Override
    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    public R unlikeNote(UnlikeNoteReqVO unlikeNoteReqVO) {
        //获取笔记ID
        Long noteId = unlikeNoteReqVO.getId();
        //当前用户ID
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            //todo
            userId = 103L;
        }

        //判断是否存在笔记
        TNote one = tNoteService.lambdaQuery().eq(TNote::getId, noteId).one();
        if (one == null)
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        //直接保存或修改数据
        // 布隆过滤器 Key
        String bloomUserNoteLikeListKey = RedisKeyConstants.buildBloomUserNoteLikeListKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_unlike_check.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Integer result = Integer.valueOf(String.valueOf(redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), noteId)));

        switch (result) {
            case 1:
                //用户点赞过
                //再去数据库判断
                TNoteLike tNoteLike = tNoteLikeService.lambdaQuery()
                        .eq(TNoteLike::getNoteId, noteId)
                        .eq(TNoteLike::getUserId, userId)
                        .eq(TNoteLike::getStatus, 1).one();
                if (tNoteLike == null) {
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_LIKED);
                }
                break;
            case 0:
                //用户没有点赞过
                throw new BizException(ResponseCodeEnum.NOTE_NOT_LIKED);
            case -1:
                //bloom过滤器不存在
                threadPoolTaskExecutor.execute(() -> {
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                    InitUserNoteLikeZSet(bloomUserNoteLikeListKey, 103L, expireSeconds);
                });
                break;
        }
        //更新Zset里面的数据
        String userNoteLikeZSetKey = RedisKeyConstants.buildUserNoteLikeZSetKey(userId);
        redisTemplate.opsForZSet().remove(userNoteLikeZSetKey, noteId);
        //通知mq
        threadPoolTaskExecutor.execute(() -> {
            LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = LikeUnlikeNoteMqDTO.builder()
                    .userId(103L)
                    .noteId(noteId)
                    .type(0)
                    .createTime(LocalDateTime.now())
                    .build();

            rabbitMqUtil.send("likeOrUnlike.exchange"
                    , MQConstants.TOPIC_LIKE_OR_UNLIKE
                    , JsonUtils.toJsonString(likeUnlikeNoteMqDTO));
        });

        return R.success();
    }

    @Override
    @SneakyThrows
    public R collectNote(CollectNoteReqVO collectNoteReqVO) {
        //获取笔记id
        Long id = collectNoteReqVO.getId();
        if (id == null)
            throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
        //获取操作者id
        Long userId = LoginUserContextHolder.getUserId();
        if (userId == null) {
            userId = 103L;
        }
        //判断笔记是否存在
        Integer result = Integer.valueOf(String.valueOf(checkNoteExist(id, userId)));
        switch (result) {
            case 1:
                //用户收藏过
                // 校验 ZSet 列表中是否包含被收藏的笔记ID
                String userNoteCollectZSetKey = RedisKeyConstants.buildUserNoteCollectZSetKey(userId);

                Double score = redisTemplate.opsForZSet().score(userNoteCollectZSetKey, id);

                if (Objects.nonNull(score)) {
                    //Zset里面存在收藏记录
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
                }

                // 若 Score 为空，则表示 ZSet 收藏列表中不存在，查询数据库校验
                Long count = tNoteCollectService.lambdaQuery()
                        .eq(TNoteCollection::getUserId, userId)
                        .eq(TNoteCollection::getNoteId, id)
                        .eq(TNoteCollection::getStatus, 1)
                        .count();

                if (count > 0) {
                    // 数据库里面有收藏记录，而 Redis 中 ZSet 已过期被删除的话，需要重新异步初始化 ZSet
                    boolean b = redisUtil.hasKey(userNoteCollectZSetKey);
                    if (!b) {
                        threadPoolTaskExecutor.execute(() -> {
                            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                            InitUserNoteCollectZSet(userNoteCollectZSetKey, 103L, expireSeconds);
                        });
                    }
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
                }
                break;
            case 0:
                //用户没有收藏过,并且已经加入了过滤器中
                break;
            case -1:
                //bloom过滤器不存在
                TNoteCollection one = tNoteCollectService.lambdaQuery()
                        .eq(TNoteCollection::getUserId, userId)
                        .eq(TNoteCollection::getStatus, 1)
                        .eq(TNoteCollection::getNoteId, id)
                        .one();
                InitCollectBloomFilter(RedisKeyConstants.buildBloomUserNoteCollectListKey(userId), userId, 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24));
                if (one != null) {
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
                } else {
                    //当前的数据加入布隆过滤器中
                    //设置过期时间
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                    // Lua 脚本路径
                    script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_note_collect_and_expire.lua")));
                    // 返回值类型
                    script.setResultType(Long.class);
                    redisTemplate.execute(script, Collections.singletonList(RedisKeyConstants.buildBloomUserNoteCollectListKey(userId)), id, expireSeconds);
                }
                break;
        }

        //更新用户 ZSET 收藏列表
        LocalDateTime now = LocalDateTime.now();
        // Lua 脚本路径
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/note_collect_check_and_update_zset.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        String userNoteCollectZSetKey = RedisKeyConstants.buildUserNoteCollectZSetKey(userId);
        result = Integer.valueOf(String.valueOf(redisTemplate.execute(script, Collections.singletonList(userNoteCollectZSetKey), id, DateUtils.localDateTime2Timestamp(now))));
        // 若 ZSet 列表不存在，需要重新初始化
        if (Objects.equals(result, -1)) {
            //重新初始化
            InitUserNoteCollectZSet(userNoteCollectZSetKey, userId, 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24));
            //加入当前的数据
            if (redisUtil.hasKey(userNoteCollectZSetKey)) {
                redisTemplate.opsForZSet().add(userNoteCollectZSetKey, id, DateUtils.localDateTime2Timestamp(now));
            } else {
                //刚才的初始化失败了,
                // 因为数据库里面没有这个用户的其他的收场记录，需要手动加入一条收藏
                DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                // Lua 脚本路径
                script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_collect_zset_and_expire.lua")));
                // 返回值类型
                script2.setResultType(Long.class);
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                List<Object> luaArgs = Lists.newArrayList();
                luaArgs.add(DateUtils.localDateTime2Timestamp(LocalDateTime.now())); // score：收藏时间戳
                luaArgs.add(id); // 当前收藏的笔记 ID
                luaArgs.add(expireSeconds); // 随机过期时间
                redisTemplate.execute(script2, Collections.singletonList(userNoteCollectZSetKey), luaArgs.toArray());
            }
        }

        // TODO: 4. 发送 MQ, 将收藏数据落库
        threadPoolTaskExecutor.execute(() -> {
            CollectUnCollectNoteMqDTO collectUnCollectNoteMqDTO = CollectUnCollectNoteMqDTO.builder()
                    .userId(103L)
                    .noteId(id)
                    .type(1)
                    .createTime(now)
                    .build();
            rabbitMqUtil.send("collectOrUncollect.exchange", MQConstants.TOPIC_COLLECT_OR_UN_COLLECT, JsonUtils.toJsonString(collectUnCollectNoteMqDTO));
        });

        return R.success();

    }

    @Override
    @SneakyThrows
    public R unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO) {
        //获取笔记id
        Long noteId = unCollectNoteReqVO.getId();
        //获取用户id
        Long userId = LoginUserContextHolder.getUserId();
        //判断笔记是否存在
        Integer result = Integer.valueOf(String.valueOf(checkNoteIsExist(noteId, userId)));
        switch (result) {
            case 1:
                //用户收藏过
            case 0:
                //用户没有收藏过
                throw new BizException(ResponseCodeEnum.NOTE_NOT_COLLECTED);
            case -1:
                //bloom过滤器不存在
                TNoteCollection one = tNoteCollectService.lambdaQuery()
                        .eq(TNoteCollection::getUserId, userId)
                        .eq(TNoteCollection::getNoteId, noteId)
                        .eq(TNoteCollection::getStatus, 1).one();
                //数据库里面也没有收藏记录
                if (one == null) {
                    InitCollectBloomFilter(RedisKeyConstants.buildBloomUserNoteCollectListKey(userId), userId, (long) (60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24)));
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_COLLECTED);
                } else {
                    //有记录
                    InitCollectBloomFilter(RedisKeyConstants.buildBloomUserNoteCollectListKey(userId), userId, (long) (60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24)));
                }
        }
        //删除 ZSET 中已收藏的笔记 ID
        // 能走到这里，说明布隆过滤器判断已收藏，直接删除 ZSET 中已收藏的笔记 ID
        // 用户收藏列表 ZSet Key
        //todo 可能zset会失效
        String userNoteCollectZSetKey = RedisKeyConstants.buildUserNoteCollectZSetKey(userId);
        redisTemplate.opsForZSet().remove(userNoteCollectZSetKey, noteId);
        // TODO: 4. 发送 MQ, 数据更新落库
        threadPoolTaskExecutor.execute(() -> {
            CollectUnCollectNoteMqDTO collectUnCollectNoteMqDTO = CollectUnCollectNoteMqDTO.builder()
                    .userId(userId)
                    .noteId(noteId)
                    .type(0)
                    .createTime(LocalDateTime.now())
                    .build();
            rabbitMqUtil.send("collectOrUncollect.exchange", MQConstants.TOPIC_COLLECT_OR_UN_COLLECT, JsonUtils.toJsonString(collectUnCollectNoteMqDTO));
        });
        return R.success();
    }

    @Override
    public R<List<FindChannelListRspVO>> findChannelList() {
        //优先查缺redis里面有没有数据
        String key = RedisKeyConstants.buildChannelListKey();
        if (redisUtil.hasKey(key)) {
            List<FindChannelListRspVO> result = new ArrayList<>();
            Map<String, Object> all = redisHashExample.getAll(key);
            all.forEach((k, v) -> {
                FindChannelListRspVO vo = new FindChannelListRspVO();
                vo.setId(Long.valueOf(String.valueOf(v)));
                vo.setName(String.valueOf(k));
                result.add(vo);
            });
            return R.success(result);
        }

        //如果不存在redis则会查询数据库
        List<TChannel> list = tChannelService.lambdaQuery().eq(TChannel::getIsDeleted, 0).list();
        //转换成vo
        List<FindChannelListRspVO> collect = list.stream().map(record -> FindChannelListRspVO.builder()
                .id(record.getId())
                .name(record.getName())
                .build()).toList();
        CompletableFuture.runAsync(() -> {
            //创建过期时间，到底一天
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            redisHashExample.putAllWithExpire(key, list.stream().collect(Collectors.toMap(TChannel::getName, TChannel::getId)), expireSeconds);
        });
        return R.success(collect);
    }

    @Override
    public R<List<FindTopicListRspVO>> findTopicList() {
        //优先查找redis
        String key = RedisKeyConstants.buildChannelAndTopicListKey();
        List<FindTopicListRspVO> result = new ArrayList<>();
        //判断redis里面是否存在
        boolean b = redisUtil.hasKey(key);
        if (b) {
            Map<String, Object> all = redisHashExample.getAll(key);
            all.forEach((k, v) -> {
                List<FindTopicListRspVO> topicList = JsonUtils.parseObject(
                        String.valueOf(v),
                        new TypeReference<List<FindTopicListRspVO>>() {
                        }
                );
                result.addAll(topicList);
            });
            return R.success(result);
        } else {
            //数据库查询
            List<TChannelTopicRel> list = tChannelTopicRelService.lambdaQuery().list();
            //按照频道id分组
            Map<Long, List<TChannelTopicRel>> collect = list.stream().collect(Collectors.groupingBy(TChannelTopicRel::getChannelId));
            collect.forEach((k, v) -> {
                //获取对应的话题id
                List<Long> topicIds = v.stream().map(TChannelTopicRel::getTopicId).toList();
                //获取对应的话题
                List<TTopic> topics = tTopicService.lambdaQuery().eq(TTopic::getIsDeleted, 0).in(TTopic::getId, topicIds).list();
                topics.forEach(topic -> {
                    FindTopicListRspVO vo = FindTopicListRspVO.builder()
                            .id(topic.getId())
                            .name(topic.getName())
                            .channelId(k)
                            .build();
                    result.add(vo);
                });
            });
            //异步存入redis
            threadPoolTaskExecutor.execute(() -> {
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                //按照频道id,对应的话题信息来分组
                Map<Long, List<FindTopicListRspVO>> redisResult = result.stream().collect(Collectors.groupingBy(FindTopicListRspVO::getChannelId));
                //存入redis
                // 转换为可存储的 Map（key 转为 String，value 序列化为 JSON）
                Map<String, String> hashMap = new HashMap<>();
                for (Map.Entry<Long, List<FindTopicListRspVO>> entry : redisResult.entrySet()) {
                    try {
                        // key：Long 转为 String（如 "1001"）
                        // value：List 序列化为 JSON 字符串
                        hashMap.put(entry.getKey().toString(), JsonUtils.toJsonString(entry.getValue()));
                    } catch (Exception e) {
                        // 处理序列化异常
                        e.printStackTrace();
                    }
                }
                try {
                    redisTemplate.opsForHash().putAll(key, hashMap);
                    redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
            return R.success(result);
        }
    }

    @Override
    public R<FindPublishedNoteListRspVO> findPublishedNoteList(FindPublishedNoteListReqVO findPublishedNoteListReqVO) throws BizException, ExecutionException, InterruptedException {
        //判断用户是否合法
        Long userId = findPublishedNoteListReqVO.getUserId();
        //获取游标
        Long cursor = findPublishedNoteListReqVO.getCursor();
        //查询数据库，查询笔记，并且按照降序的排序
        List<TNote> result = tNoteService.selectPublishedNoteListByUserIdAndCursor(userId, cursor);
        if (result == null || result.size() == 0) {
            //没有数据
            return R.success(FindPublishedNoteListRspVO.builder().notes(null).nextCursor(null).build());
        } else {
            //采集所有的笔记Id
            List<Long> noteIds = result.stream().map(TNote::getId).toList();
            CompletableFuture<Map<Long, FindNoteCountsByIdRspDTO>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    List<FindNoteCountsByIdRspDTO> notesCountData = countRpcService.findNotesCountData(noteIds);
                    return notesCountData.stream().collect(Collectors.toMap(FindNoteCountsByIdRspDTO::getNoteId, p -> p));
                } catch (BizException e) {
                    //返回空队列
                    return new HashMap<>();
                }
            });

            FindPublishedNoteListRspVO findPublishedNoteListRspVO = new FindPublishedNoteListRspVO();
            FindUserByIdRspDTO findUserByIdRspDTO = userRpcService.findById(userId);
            Map<Long, FindNoteCountsByIdRspDTO> longFindNoteCountsByIdRspDTOMap = future.get();
            //查询该用户的笔记
            List<NoteItemRspVO> notes = result.stream().map(sign -> {
                NoteItemRspVO vo = new NoteItemRspVO();
                vo.setNoteId(sign.getId());
                vo.setType(sign.getType());
                // 获取封面图片
                String cover = StringUtils.isNotBlank(sign.getImgUris()) ?
                        StringUtils.split(sign.getImgUris(), ",")[0] : null;
                vo.setCover(cover);
                vo.setVideoUri(sign.getVideoUri());
                vo.setTitle(sign.getTitle());
                vo.setCreatorId(sign.getCreatorId());
                vo.setNickname(findUserByIdRspDTO.getNickName());
                vo.setAvatar(findUserByIdRspDTO.getAvatar());
                vo.setLikeTotal(String.valueOf(longFindNoteCountsByIdRspDTOMap.get(sign.getId()).getLikeTotal()));
                return vo;
            }).toList();
            //根据笔记的创建时间，获取最晚发布笔记id
            Optional<TNote> min = result.stream().min(Comparator.comparing(TNote::getCreateTime));
            cursor = min.get().getId();
            findPublishedNoteListRspVO.setNotes(notes);
            findPublishedNoteListRspVO.setNextCursor(cursor);
            //异步存入redis


            return R.success(findPublishedNoteListRspVO);
        }
    }

    private Long checkNoteIsExist(Long noteId, Long userId) throws BizException {
        //先从数据库查询笔记是否存在
        TNote one = tNoteService.lambdaQuery().eq(TNote::getId, noteId).one();
        if (one == null)
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);

        //判断布隆器里面是否已经点赞了
        String bloomUserNoteCollectListKey = RedisKeyConstants.buildBloomUserNoteCollectListKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_uncollect_check.lua")));
        script.setResultType(Long.class);
        Long result = (Long) redisTemplate.execute(script, Collections.singletonList(bloomUserNoteCollectListKey), noteId);
        return result;
    }

    private void InitUserNoteCollectZSet(String userNoteCollectZSetKey, Long userId, long expireSeconds) {
        try {
            List<TNoteCollection> noteCollectionDOS = tNoteCollectService.lambdaQuery()
                    .eq(TNoteCollection::getUserId, userId)
                    .eq(TNoteCollection::getStatus, 1)
                    .orderByDesc(TNoteCollection::getCreateTime)
                    .last("limit 300").list();
            //如果没有数据
            if (CollUtil.isEmpty(noteCollectionDOS)) {
                return;
            }

            int argsLength = noteCollectionDOS.size() * 2 + 1; // 每个笔记收藏关系有 2 个参数（score 和 value），最后再跟一个过期时间
            Object[] luaArgs = new Object[argsLength];

            int i = 0;
            for (TNoteCollection noteCollectionDO : noteCollectionDOS) {
                luaArgs[i] = DateUtils.localDateTime2Timestamp(noteCollectionDO.getCreateTime()); // 收藏时间作为 score
                luaArgs[i + 1] = noteCollectionDO.getNoteId();          // 笔记ID 作为 ZSet value
                i += 2;
            }

            luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间

            DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
            // Lua 脚本路径
            script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_collect_zset_and_expire.lua")));
            // 返回值类型
            script2.setResultType(Long.class);

            redisTemplate.execute(script2, Collections.singletonList(userNoteCollectZSetKey), luaArgs);
        } catch (Exception e) {
            log.error("## 异步初始化 ZSet 异常: ", e);
        }
    }

    private void InitCollectBloomFilter(String bloomUserNoteCollectListKey, Long userId, long l) {
        //开始初始化bloom过滤器
        List<TNoteCollection> list = tNoteCollectService.lambdaQuery().eq(TNoteCollection::getUserId, userId).eq(TNoteCollection::getStatus, 1).list();
        if (!list.isEmpty()) {
            try {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                // Lua 脚本路径
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_note_collect_and_expire.lua")));
                // 返回值类型
                script.setResultType(Long.class);
                // 构建 Lua 参数
                List<Object> luaArgs = Lists.newArrayList();
                list.forEach(noteCollectionDO -> luaArgs.add(noteCollectionDO.getNoteId())); // 将每个收藏的笔记 ID 传入
                //设置过期时间
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                redisTemplate.execute(script, Collections.singletonList(bloomUserNoteCollectListKey), luaArgs.toArray());
            } catch (Exception e) {

            }
        }
    }


    private Long checkNoteExist(Long noteId, Long userId) throws BizException {
        //先从数据库查询笔记是否存在
        TNote one = tNoteService.lambdaQuery().eq(TNote::getId, noteId).one();
        if (one == null)
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);

        //构建key
        String key = RedisKeyConstants.buildNoteDetailKey(userId);
        // 布隆过滤器 Key
        String bloomUserNoteCollectListKey = RedisKeyConstants.buildBloomUserNoteCollectListKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_collect_check.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Long result = (Long) redisTemplate.execute(script, Collections.singletonList(bloomUserNoteCollectListKey), noteId);

        return result;
    }

    private void InitUserNoteLikeZSet(String userNoteLikeZSetKey, Long userId, long expireSeconds) {
        //查询该用户的点赞情况,并且只查出最近点赞的一百条
        List<TNoteLike> list = tNoteLikeService.lambdaQuery()
                .eq(TNoteLike::getUserId, userId)
                .orderByDesc(TNoteLike::getCreateTime)
                .last("limit 100").list();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
        // Lua 脚本路径
        script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_like_zset_and_expire.lua")));
        // 返回值类型
        script2.setResultType(Long.class);
        if (CollUtil.isNotEmpty(list)) {
            //有数据存在
            // 构建 Lua 参数
            Object[] luaArgs = buildNoteLikeZSetLuaArgs(list, expireSeconds);
            //创建Zset
            redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs);
        }

    }

    private Object[] buildNoteLikeZSetLuaArgs(List<TNoteLike> noteLikeDOS, long expireSeconds) {
        int argsLength = noteLikeDOS.size() * 2 + 1; // 每个笔记点赞关系有 2 个参数（score 和 value），最后再跟一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (TNoteLike noteLikeDO : noteLikeDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(noteLikeDO.getCreateTime()); // 点赞时间作为 score
            luaArgs[i + 1] = noteLikeDO.getNoteId();          // 笔记ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }

    private void InitBloomFilter(String bloomUserNoteLikeListKey, Long userId, Long expireSeconds) {
        // 从数据库中查询用户点赞的笔记列表
        List<TNoteLike> noteLikes = tNoteLikeService.lambdaQuery()
                .eq(TNoteLike::getUserId, userId)
                .eq(TNoteLike::getStatus, 1).list();
        if (CollUtil.isNotEmpty(noteLikes)) {
            // 构建 Lua 参数
            try {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                // Lua 脚本路径
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_note_like_and_expire.lua")));
                // 返回值类型
                script.setResultType(Long.class);
                // 构建 Lua 参数
                List<Object> luaArgs = Lists.newArrayList();
                noteLikes.forEach(noteLikeDO -> luaArgs.add(noteLikeDO.getNoteId())); // 将每个点赞的笔记 ID 传入
                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), luaArgs.toArray());
            } catch (Exception e) {
                log.error("## 异步初始化布隆过滤器异常: ", e);
            }
        }
    }

    private void handleExistNote(LikeNoteReqVO likeNoteReqVO) throws BizException {
        //获取笔记id
        Long noteId = likeNoteReqVO.getId();
        if (Objects.isNull(tNoteService.getById(noteId))) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }
        //构建KEY
        String key = RedisKeyConstants.buildNoteDetailKey(noteId);
        Object o = redisUtil.get(key);
        if (o != null) {
            FindNoteDetailRspVO findNoteDetailRspVO = JsonUtils.parseObject(String.valueOf(o), FindNoteDetailRspVO.class);

        } else {
            //redis里面不存在，再从数据库里面查询
            TNote one = tNoteService.lambdaQuery()
                    .eq(TNote::getId, noteId)
                    .eq(TNote::getStatus, NoteStatusEnum.NORMAL.getCode())
                    .one();
            if (Objects.isNull(one)) {
                throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
            }
            //数据库里面存在则存入redis里面
            threadPoolTaskExecutor.execute(() -> {
                try {
                    //随机过期时间
                    redisUtil.set(key, JsonUtils.toJsonString(one), 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24));
                } catch (Exception e) {
                    log.error("异步存储Redis失败，key:{}", key, e);
                }
            });
        }
    }


    /**
     * 校验笔记的可见性
     *
     * @param visible    是否可见
     * @param currUserId 当前用户 ID
     * @param creatorId  笔记创建者
     */
    private void checkNoteVisible(Integer visible, Long currUserId, Long creatorId) throws BizException {
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !Objects.equals(currUserId, creatorId)) {
            // 仅自己可见, 并且访问用户为笔记创建者才能访问，非本人则抛出异常
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }
    }
}


