package com.zhangzc.bookcountbiz.Service.impl;

import cn.hutool.core.util.RandomUtil;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountbiz.Const.RedisKeyConstants;
import com.zhangzc.bookcountbiz.Pojo.Domain.TNoteCount;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdRspDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdsReqDTO;
import com.zhangzc.bookcountbiz.Service.NoteCountService;
import com.zhangzc.bookcountbiz.Service.TNoteCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class NoteCountServiceImpl implements NoteCountService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final TNoteCountService tNoteCountService;

    @Override
    public R<List<FindNoteCountsByIdRspDTO>> findNotesCountData(FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO) {
        //获取笔记ids
        List<Long> noteIds = findNoteCountsByIdsReqDTO.getNoteIds();
        //如果为空
        if (noteIds == null || noteIds.isEmpty()) {
            return R.success();
        }
        //从redis里面获取
        List<Long> noteIds1 = findNoteCountsByIdsReqDTO.getNoteIds();
        //构建redisKeys
        List<String> redisKeys = noteIds1.stream().map(RedisKeyConstants::buildCountNoteKey).toList();
        // 使用 Pipeline 通道，从 Redis 中批量查询笔记 Hash 计数
        List<Object> countHashes = getCountHashesByPipelineFromRedis(redisKeys);
        //记录索引
        AtomicInteger index = new AtomicInteger(0);
        List<FindNoteCountsByIdRspDTO> redisResults = countHashes.stream().map(sign -> {
            FindNoteCountsByIdRspDTO findNoteCountsByIdRspDTO = new FindNoteCountsByIdRspDTO();
            //转换为List<Long>
            List<Long> sign1 = (List<Long>) sign;
            //是否需要查询数据库
            boolean needQueryDB = false;
            //笔记的id
            Long noteId = noteIds1.get(index.getAndIncrement());
            //点赞数
            Long likeTotal = (sign1 == null || sign1.get(0) == null) ? null : Long.valueOf(String.valueOf(sign1.get(0)));
            if (likeTotal == null) {
                needQueryDB = true;
            }
            //评论数
            Long commentTotal = (sign1 == null || sign1.get(1) == null) ? null : Long.valueOf(String.valueOf(sign1.get(1)));
            if (commentTotal == null) {
                needQueryDB = true;
            }
            //收藏数
            Long collectTotal = (sign1 == null || sign1.get(2) == null) ? null : Long.valueOf(String.valueOf(sign1.get(2)));
            if (collectTotal == null) {
                needQueryDB = true;
            }
            if (needQueryDB) {
                //从数据库查询
                List<Long> counts = new ArrayList<>(Arrays.asList(likeTotal, collectTotal, commentTotal));
                setNotecounts(counts, noteId);
                // 更新从数据库获取的值
                likeTotal = counts.get(0) == null ? 0 : counts.get(0);
                collectTotal = counts.get(1) == null ? 0 : counts.get(1);
                commentTotal = counts.get(2) == null ? 0 : counts.get(2);
            }
            findNoteCountsByIdRspDTO.setNoteId(noteId);
            findNoteCountsByIdRspDTO.setLikeTotal(likeTotal);
            findNoteCountsByIdRspDTO.setCollectTotal(collectTotal);
            findNoteCountsByIdRspDTO.setCommentTotal(commentTotal);
            return findNoteCountsByIdRspDTO;
        }).toList();
        return R.success(redisResults);
    }

    private void setNotecounts(List<Long> sign, Long noteId) {
        //从数据库查询
        //数据库查询
        System.out.println(noteId + "开始查询");
        TNoteCount one = tNoteCountService.lambdaQuery()
                .eq(TNoteCount::getNoteId, noteId).one();
        if (Objects.nonNull(one)) {
            sign.set(0, one.getLikeTotal());
            sign.set(1, one.getCollectTotal());
            sign.set(2, one.getCommentTotal());
        } else {
            sign.set(0, 0L);
            sign.set(1, 0L);
            sign.set(2, 0L);
        }
        //更新redis
        CompletableFuture.runAsync(() -> {
            //构建redisKey
            String redisKey = RedisKeyConstants.buildCountNoteKey(noteId);
            //获取随机的过期时间保底是一天
            long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            redisTemplate.opsForHash().putAll(redisKey, Map.of(
                    RedisKeyConstants.FIELD_LIKE_TOTAL, one == null || one.getLikeTotal() == null ? 0L : one.getLikeTotal(),
                    RedisKeyConstants.FIELD_COLLECT_TOTAL, one == null || one.getCollectTotal() == null ? 0L : one.getCollectTotal(),
                    RedisKeyConstants.FIELD_COMMENT_TOTAL, one == null || one.getCommentTotal() == null ? 0L : one.getCommentTotal()
            ));
            redisTemplate.expire(redisKey, expireTime, TimeUnit.SECONDS);
        });

    }


    private List<Object> getCountHashesByPipelineFromRedis(List<String> redisKeys) {
        // 使用 Pipeline 通道，从 Redis 中批量查询笔记 Hash 计数
        return redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                for (String hashKey : redisKeys) {
                    // 批量获取多个字段
                    operations.opsForHash().multiGet(hashKey, List.of(
                            RedisKeyConstants.FIELD_LIKE_TOTAL,
                            RedisKeyConstants.FIELD_COLLECT_TOTAL,
                            RedisKeyConstants.FIELD_COMMENT_TOTAL
                    ));
                }
                return null;
            }
        });
    }
}
