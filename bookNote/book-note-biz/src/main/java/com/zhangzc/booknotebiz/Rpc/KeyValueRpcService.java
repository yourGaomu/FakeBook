package com.zhangzc.booknotebiz.Rpc;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Api.KeyValueFeignApi;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.AddNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.DeleteNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.FindNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindNoteContentRspDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KeyValueRpcService {
    private final KeyValueFeignApi keyValueFeignApi;

    public boolean saveNoteContent(String contentUuid, String content) {
        AddNoteContentReqDTO addNoteContentReqDTO = AddNoteContentReqDTO.builder()
                .uuid(contentUuid)
                .content(content)
                .build();

        R response = keyValueFeignApi.addNoteContent(addNoteContentReqDTO);

        return response.isSuccess();
    }

    public void deleteNoteContent(String contentUuid) {
        keyValueFeignApi.deleteNoteContent(DeleteNoteContentReqDTO.builder()
                .uuid(contentUuid)
                .build());
    }

    public String findNoteContent(String uuid) {
        FindNoteContentReqDTO findNoteContentReqDTO = new FindNoteContentReqDTO();
        findNoteContentReqDTO.setUuid(uuid);

        R<FindNoteContentRspDTO> response = keyValueFeignApi.findNoteContent(findNoteContentReqDTO);

        if (Objects.isNull(response) || !response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }

        return response.getData().getContent();
    }

}
