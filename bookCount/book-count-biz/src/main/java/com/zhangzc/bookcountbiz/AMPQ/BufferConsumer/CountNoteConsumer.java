package com.zhangzc.bookcountbiz.AMPQ.BufferConsumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhangzc.bookcountbiz.Const.RedisKeyConstants;
import com.zhangzc.bookcountbiz.Pojo.Domain.TNoteCount;
import com.zhangzc.bookcountbiz.Pojo.Domain.TNoteLike;
import com.zhangzc.bookcountbiz.Pojo.Dto.CountLikeUnlikeNoteMqDTO;
import com.zhangzc.bookcountbiz.Service.TNoteCountService;
import com.zhangzc.bookcountbiz.Service.TNoteLikeService;
import com.zhangzc.fakebookspringbootstartjackon.Utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountNoteConsumer {
    private final TNoteCountService tNoteCountService;

    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final RedisTemplate<String, Object> redisTemplate;

    public void consumeCountMessage(List<String> bodies) {
        threadPoolTaskExecutor.execute(() -> {
            //开始序列化
            List<CountLikeUnlikeNoteMqDTO> list = bodies.stream().map(body -> JsonUtils.parseObject(body, CountLikeUnlikeNoteMqDTO.class)).toList();
            //按照笔记ID操作
            Map<Long, List<CountLikeUnlikeNoteMqDTO>> collect = list.stream().collect(Collectors.groupingBy(CountLikeUnlikeNoteMqDTO::getUserId));

            //开始入库操作
            collect.forEach((noteId, dtoList) -> {
                handleCountMessage(noteId, dtoList);
            });
        });
    }

    private void handleCountMessage(Long noteId, List<CountLikeUnlikeNoteMqDTO> dtoList) {
        //用户为空
        if (noteId == null) {
            return;
        }
        try {
            //数据不为空
            if (dtoList.size() >= 1) {
                //统计对这篇笔记的点赞和取消赞的总数
                int likeCount = 0;
                int unlikeCount = 0;
                for (CountLikeUnlikeNoteMqDTO dto : dtoList) {
                    if (dto.getType() == 1) {
                        likeCount++;
                    } else if (dto.getType() == 0) {
                        unlikeCount++;
                    }
                }
                int total = likeCount - unlikeCount;
                //开始修改redis数据
                String redisKey = RedisKeyConstants.buildCountNoteKey(noteId);
                // 判断 Redis 中 Hash 是否存在
                boolean isExisted = redisTemplate.hasKey(redisKey);

                // 若存在才会更新
                // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
                if (isExisted) {
                    // 对目标用户 Hash 中的点赞数字段进行计数操作
                    redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_LIKE_TOTAL, total);
                }
                //开始入数据库操作
                tNoteCountService.incrementLikeTotal(noteId, total);
            }
        }catch (Exception e){
            log.error("==> 转换计数数据失败，message:{}", noteId, e);
        }
    }
}

