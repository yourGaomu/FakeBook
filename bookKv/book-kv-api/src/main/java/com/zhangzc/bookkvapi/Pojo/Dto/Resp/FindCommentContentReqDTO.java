package com.zhangzc.bookkvapi.Pojo.Dto.Resp;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentContentReqDTO {


    private String yearMonth;


    private String contentId;

}
