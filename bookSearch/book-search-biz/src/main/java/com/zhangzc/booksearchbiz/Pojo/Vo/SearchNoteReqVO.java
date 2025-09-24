package com.zhangzc.booksearchbiz.Pojo.Vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchNoteReqVO {

    private String keyword;


    private Integer pageNo = 1; // 默认值为第一页

}
