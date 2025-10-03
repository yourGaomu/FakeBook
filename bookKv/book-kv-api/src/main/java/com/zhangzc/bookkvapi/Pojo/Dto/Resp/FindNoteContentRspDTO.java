package com.zhangzc.bookkvapi.Pojo.Dto.Resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteContentRspDTO {

    /**
     * 笔记内容 ID
     */
    private UUID noteId;

    /**
     * 笔记内容
     */
    private String content;

}

