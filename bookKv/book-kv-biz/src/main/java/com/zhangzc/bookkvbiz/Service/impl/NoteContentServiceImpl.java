package com.zhangzc.bookkvbiz.Service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.AddNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.DeleteNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Req.FindNoteContentReqDTO;
import com.zhangzc.bookkvapi.Pojo.Dto.Resp.FindNoteContentRspDTO;
import com.zhangzc.bookkvbiz.Enums.ResponseCodeEnum;
import com.zhangzc.bookkvbiz.Pojo.Domain.TNoteContent;
import com.zhangzc.bookkvbiz.Service.NoteContentService;
import com.zhangzc.bookkvbiz.Service.TNoteContentService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoteContentServiceImpl implements NoteContentService {

    private final TNoteContentService tNoteContentService;
    @Override
    public R addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {

        // 笔记 ID
        Long noteId = addNoteContentReqDTO.getNoteId();
        // 笔记内容
        String content = addNoteContentReqDTO.getContent();

        // 构建数据库 DO 实体类
        TNoteContent nodeContent = TNoteContent.builder()
                .id(String.valueOf(UUID.randomUUID())) // TODO: 暂时用 UUID, 目的是为了下一章讲解压测，不用动态传笔记 ID。后续改为笔记服务传过来的笔记 ID
                .content(content)
                .build();

        // 插入数据
        tNoteContentService.save(nodeContent);

        return R.success();

    }

    @Override
    @SneakyThrows
    public R<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO) {
        FindNoteContentRspDTO findNoteContentRspDTO = new FindNoteContentRspDTO();

        String noteId = findNoteContentReqDTO.getNoteId();

        TNoteContent tNoteContent = tNoteContentService.lambdaQuery().eq(TNoteContent::getId, noteId).list().get(0);

        if (tNoteContent == null) {
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }

        findNoteContentRspDTO.setContent(tNoteContent.getContent());
        findNoteContentRspDTO.setNoteId(java.util.UUID.fromString(tNoteContent.getId()));

        return R.success(findNoteContentRspDTO);

    }

    @Override
    @SneakyThrows
    public R deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        String noteId = deleteNoteContentReqDTO.getNoteId();
        LambdaQueryWrapper<TNoteContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TNoteContent::getId, noteId);
        boolean remove = tNoteContentService.remove(queryWrapper);
        if (!remove){
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_DELETE_FAIL);
        }
        return R.success();
    }
}
