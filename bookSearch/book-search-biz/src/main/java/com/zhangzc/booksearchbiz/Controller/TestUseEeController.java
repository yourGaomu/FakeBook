package com.zhangzc.booksearchbiz.Controller;

import com.zhangzc.booksearchbiz.Mapper.Es.DocumentMapper;
import com.zhangzc.booksearchbiz.Mapper.Es.SearchNoteMapper;
import com.zhangzc.booksearchbiz.Pojo.Vo.Document;
import com.zhangzc.booksearchbiz.Pojo.Vo.SearchNoteRspVO;
import lombok.RequiredArgsConstructor;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestUseEeController {
    final DocumentMapper documentMapper;
    final SearchNoteMapper searchNoteMapper;
    private final SearchNoteStringDateTestGenerator searchNoteStringDateTestGenerator;

    @GetMapping("/insert")
    public Integer insert() {
        List<SearchNoteRspVO> searchNoteRspVOS = searchNoteStringDateTestGenerator.generateTestData(100);
        return searchNoteMapper.insertBatch(searchNoteRspVOS);
    }

    @GetMapping("/create")
    public Integer create() {
        searchNoteMapper.createIndex();
        return 1;
    }

    @GetMapping("/search")
    public List<Document> search() {
        // 查询出所有标题为老汉的文档列表
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "术过");
        return documentMapper.selectList(wrapper);
    }
}
