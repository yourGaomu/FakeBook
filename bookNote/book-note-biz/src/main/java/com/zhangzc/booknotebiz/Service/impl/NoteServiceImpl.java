package com.zhangzc.booknotebiz.Service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.google.common.base.Preconditions;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcommon.Utils.TimeUtil;
import com.zhangzc.booknotebiz.Const.MQConstants;
import com.zhangzc.booknotebiz.Const.RedisKeyConstants;
import com.zhangzc.booknotebiz.Enum.NoteStatusEnum;
import com.zhangzc.booknotebiz.Enum.NoteTypeEnum;
import com.zhangzc.booknotebiz.Enum.NoteVisibleEnum;
import com.zhangzc.booknotebiz.Enum.ResponseCodeEnum;
import com.zhangzc.booknotebiz.Pojo.Domain.TNote;
import com.zhangzc.booknotebiz.Pojo.Domain.TNoteContent;
import com.zhangzc.booknotebiz.Pojo.Domain.TTopic;
import com.zhangzc.booknotebiz.Pojo.Vo.*;
import com.zhangzc.booknotebiz.Rpc.DistributedIdGeneratorRpcService;
import com.zhangzc.booknotebiz.Rpc.KeyValueRpcService;
import com.zhangzc.booknotebiz.Rpc.UserRpcService;
import com.zhangzc.booknotebiz.Service.NoteService;
import com.zhangzc.booknotebiz.Service.TNoteContentService;
import com.zhangzc.booknotebiz.Service.TNoteService;
import com.zhangzc.booknotebiz.Service.TTopicService;
import com.zhangzc.booknotebiz.Utils.RabbitMqUtil;
import com.zhangzc.booknotebiz.Utils.RedisUtil;
import com.zhangzc.bookuserapi.Pojo.Dto.Resp.FindUserByIdRspDTO;
import com.zhangzc.fakebookspringbootstartcontext.Const.LoginUserContextHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;


import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    private final UserRpcService userRpcService;
    private final TTopicService tTopicService;
    private final TNoteService tNoteService;
    private final TNoteContentService tNoteContentService;
    private final KeyValueRpcService keyValueRpcService;
    private final RedisUtil redisUtil;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RabbitMqUtil rabbitMqUtil;

    // 手动编写构造函数，为线程池参数添加 @Qualifier
    public NoteServiceImpl(
            DistributedIdGeneratorRpcService distributedIdGeneratorRpcService,
            UserRpcService userRpcService,
            TTopicService tTopicService,
            TNoteService tNoteService,
            TNoteContentService tNoteContentService,
            KeyValueRpcService keyValueRpcService,
            RedisUtil redisUtil,
            @Qualifier("taskExecutor") ThreadPoolTaskExecutor threadPoolTaskExecutor,
            RabbitMqUtil rabbitMqUtil
    ) {
        this.distributedIdGeneratorRpcService = distributedIdGeneratorRpcService;
        this.userRpcService = userRpcService;
        this.tTopicService = tTopicService;
        this.tNoteService = tNoteService;
        this.tNoteContentService = tNoteContentService;
        this.keyValueRpcService = keyValueRpcService;
        this.redisUtil = redisUtil;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.rabbitMqUtil = rabbitMqUtil;
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

        // 删除缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisUtil.del(noteDetailRedisKey);

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rabbitMqUtil.sendDelayMessage("delay.exchange", MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteDetailRedisKey, 3L);
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


