package com.zhangzc.bookcountbiz.Mapper;

import com.zhangzc.bookcountbiz.Pojo.Domain.TUserCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangzc.bookcountbiz.Pojo.Dto.PublishNoteMqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author 吃饭
* @description 针对表【t_user_count(用户计数表)】的数据库操作Mapper
* @createDate 2025-09-07 15:24:39
* @Entity com.zhangzc.bookcountbiz.Pojo.Domain.TUserCount
*/
public interface TUserCountMapper extends BaseMapper<TUserCount> {

    void saveOrUpdataBatch(Map<Long, Long> result);

    void incrementLikeTotal(@Param("userID") Long userId, @Param("total") int total);

    void incrementPublishTotalBatch(List<PublishNoteMqDTO> publishNoteMqDTOS);
}




