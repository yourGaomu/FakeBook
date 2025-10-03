package com.zhangzc.booknotebiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindProfileNoteListReqVO {
    private Long pageNo;
    private Long type;
    private Long userId;
}
