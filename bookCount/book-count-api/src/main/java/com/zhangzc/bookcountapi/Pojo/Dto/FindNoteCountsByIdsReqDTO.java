package com.zhangzc.bookcountapi.Pojo.Dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindNoteCountsByIdsReqDTO {
    /**
     * 笔记 ID 列表
     */
    private List<Long> noteIds;

}
