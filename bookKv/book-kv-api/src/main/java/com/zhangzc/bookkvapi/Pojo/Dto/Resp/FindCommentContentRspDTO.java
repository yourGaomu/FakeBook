package com.zhangzc.bookkvapi.Pojo.Dto.Resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentContentRspDTO {

    /**
     * 评论内容 UUID
     */
    private String contentId;

    /**
     * 评论内容
     */
    private String content;

}
