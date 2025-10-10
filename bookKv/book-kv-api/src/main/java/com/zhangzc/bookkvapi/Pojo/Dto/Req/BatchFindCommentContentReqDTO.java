package com.zhangzc.bookkvapi.Pojo.Dto.Req;


import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindCommentContentReqDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchFindCommentContentReqDTO {

    /**
     * 笔记 ID
     */

    private Long noteId;

    private List<FindCommentContentReqDTO> commentContentKeys;

}
