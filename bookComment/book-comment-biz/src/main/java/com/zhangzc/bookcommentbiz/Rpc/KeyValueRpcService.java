package com.zhangzc.bookcommentbiz.Rpc;

import cn.hutool.core.collection.CollUtil;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Api.KeyValueFeignApi;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.BatchFindCommentContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindCommentContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindCommentContentRspDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeyValueRpcService {

    private final KeyValueFeignApi keyValueFeignApi;

    /**
     * 批量查询评论内容
     * @param noteId
     * @param findCommentContentReqDTOS
     * @return
     */
    public List<FindCommentContentRspDTO> batchFindCommentContent(Long noteId, List<FindCommentContentReqDTO> findCommentContentReqDTOS) {
        BatchFindCommentContentReqDTO bathFindCommentContentReqDTO = BatchFindCommentContentReqDTO.builder()
                .noteId(noteId)
                .commentContentKeys(findCommentContentReqDTOS)
                .build();

        R<List<FindCommentContentRspDTO>> response = keyValueFeignApi.batchFindCommentContent(bathFindCommentContentReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }

        return response.getData();
    }


}

