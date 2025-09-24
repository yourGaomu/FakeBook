package com.zhangzc.booksearchbiz.Controller;

import com.zhangzc.booksearchbiz.Mapper.DocumentMapper;
import com.zhangzc.booksearchbiz.Pojo.Vo.Document;
import lombok.RequiredArgsConstructor;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestUseEeController {
     final DocumentMapper documentMapper;
    
    @GetMapping("/insert")
    public Integer insert() {
        // 初始化-> 新增数据
        Document document = new Document();
        document.setTitle("老汉");


        document.setContent("推*技术过硬");
        return documentMapper.insert(document);
    }

    @GetMapping("/search")
    public List<Document> search() {
        // 查询出所有标题为老汉的文档列表
        LambdaEsQueryWrapper<Document> wrapper = new LambdaEsQueryWrapper<>();
        wrapper.match(Document::getContent, "术过");
        return documentMapper.selectList(wrapper);
    }
}
