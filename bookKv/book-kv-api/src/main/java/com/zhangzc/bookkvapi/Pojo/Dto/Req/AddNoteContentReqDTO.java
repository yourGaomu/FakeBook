package com.zhangzc.bookkvapi.Pojo.Dto.Req;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddNoteContentReqDTO {


    private Long noteId;


    private String content;

}

