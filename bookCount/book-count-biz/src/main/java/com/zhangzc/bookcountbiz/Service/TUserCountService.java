package com.zhangzc.bookcountbiz.Service;

import com.zhangzc.bookcountbiz.Pojo.Domain.TUserCount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhangzc.bookcountbiz.Pojo.Dto.PublishNoteMqDTO;

import java.util.List;
import java.util.Map;

/**
* @author 吃饭
* @description 针对表【t_user_count(用户计数表)】的数据库操作Service
* @createDate 2025-09-07 15:24:39
*/
public interface TUserCountService extends IService<TUserCount> {

    void saveOrUpdataBatch(Map<String, Long> result);

    void incrementLikeTotal(Long userId, int total);

    void incrementPublishTotalBatch(List<PublishNoteMqDTO> publishNoteMqDTOS);

}
