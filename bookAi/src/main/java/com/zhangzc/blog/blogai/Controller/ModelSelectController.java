package com.zhangzc.blog.blogai.Controller;



import com.zhangzc.blog.blogai.Pojo.Vo.Models;


import com.zhangzc.blog.blogai.Pojo.domain.TLlmModel;

import com.zhangzc.blog.blogai.Service.TLlmModelService;


import com.zhangzc.bookcommon.Utils.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/model/")
@RequiredArgsConstructor
public class ModelSelectController {
    private final TLlmModelService tLlmModelService;

    @PostMapping("getModelSelect")
    public R<Models> getModelSelect() {
        List<TLlmModel> list = tLlmModelService.lambdaQuery()
                .eq(TLlmModel::getIsEnable, 1)
                .list();

        Map<String, List<Models.Model>> grouped = list.stream()
                .collect(Collectors.groupingBy(
                        item -> {
                            String type = item.getModelType();
                            return type == null || type.isBlank() ? "unknown" : type;
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(item -> {
                            Models.Model model = new Models.Model();
                            model.setId(item.getId());
                            model.setModelName(item.getModelName());
                            model.setModelCode(item.getModelCode());
                            model.setModelType(item.getModelType());
                            return model;
                        }, Collectors.toList())
                ));

        return R.success(new Models(grouped));
    }



}
