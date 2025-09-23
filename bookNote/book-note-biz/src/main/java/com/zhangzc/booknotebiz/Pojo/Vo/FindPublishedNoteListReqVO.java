package com.zhangzc.booknotebiz.Pojo.Vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindPublishedNoteListReqVO {

    private Long userId;

    /**
     * 游标，即笔记 ID，用于分页使用
     */
    private Long cursor;

}
