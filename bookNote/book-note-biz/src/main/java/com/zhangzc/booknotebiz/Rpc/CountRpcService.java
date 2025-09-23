package com.zhangzc.booknotebiz.Rpc;

import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookcountapi.Api.CountFeginApi;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdRspDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.FindNoteCountsByIdsReqDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Req.FindUserCountsByIdReqDTO;
import com.zhangzc.bookcountapi.Pojo.Dto.Resp.FindUserCountsByIdRspDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountRpcService {
    private final CountFeginApi countFeginApi;

    public FindUserCountsByIdRspDTO findUserCountsByIdRspDTO(Long userID) throws BizException {
        return countFeginApi.findUserCountData(FindUserCountsByIdReqDTO.builder().userId(userID).build()).getData();
    }

    public List<FindNoteCountsByIdRspDTO> findNotesCountData(List<Long> noteIds) throws BizException {
        if (noteIds == null || noteIds.isEmpty()) {
            return new ArrayList<>();
        }
        FindNoteCountsByIdsReqDTO build = new FindNoteCountsByIdsReqDTO();
        build.setNoteIds(noteIds);
        R<List<FindNoteCountsByIdRspDTO>> notesCountData = countFeginApi.findNotesCountData(build);
        //修改对应的程序
        List<FindNoteCountsByIdRspDTO> data = notesCountData.getData();
        return data;
    }
}

