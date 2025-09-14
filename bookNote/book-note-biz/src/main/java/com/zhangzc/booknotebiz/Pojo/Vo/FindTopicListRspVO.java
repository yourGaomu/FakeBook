package com.zhangzc.booknotebiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindTopicListRspVO {
    private Long id;
    private String name;
    private Long channelId;  // 关联频道
}
