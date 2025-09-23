package com.zhangzc.bookcountbiz.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountbiz.Pojo.Domain.TUserCount;
import com.zhangzc.bookcountbiz.Pojo.Dto.PublishNoteMqDTO;
import com.zhangzc.bookcountbiz.Rpc.NoteRpcService;
import com.zhangzc.bookcountbiz.Service.TUserCountService;
import com.zhangzc.bookcountbiz.Mapper.TUserCountMapper;
import com.zhangzc.booknoteapi.Pojo.Dto.Resp.FindNoteDetailRspVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 吃饭
 * @description 针对表【t_user_count(用户计数表)】的数据库操作Service实现
 * @createDate 2025-09-07 15:24:39
 */
@Service
@RequiredArgsConstructor
public class TUserCountServiceImpl extends ServiceImpl<TUserCountMapper, TUserCount>
        implements TUserCountService {

    private final NoteRpcService noteRpcService;

    @Override
    public void saveOrUpdataBatch(Map<String, Long> result) {
        //如果数据为空
        if (result == null || result.isEmpty()) {
            return;
        }
        //修改第一个key为Long类型
        Map<Long, Long> collect = result.entrySet().stream()
                .map(sign -> {
                    R<FindNoteDetailRspVO> noteDetail = noteRpcService.findNoteDetail(Long.parseLong(sign.getKey()));
                    Long creatorId = noteDetail.getData().getCreatorId();
                    if (creatorId == null) {
                        return null;
                    } else {
                        return Map.entry(creatorId, sign.getValue());
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        //第一个参数是用户id,第二个参数是变化量
        this.baseMapper.saveOrUpdataBatch(collect);
    }

    @Override
    public void incrementLikeTotal(Long userId, int total) {
        //判断数据是否合法
        if (userId == null || total <= 0) {
            return;
        }
        this.baseMapper.incrementLikeTotal(userId, total);
    }

    @Override
    public void incrementPublishTotalBatch(List<PublishNoteMqDTO> publishNoteMqDTOS) {
        this.baseMapper.incrementPublishTotalBatch(publishNoteMqDTOS);
    }
}




