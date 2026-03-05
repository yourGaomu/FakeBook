package com.zhangzc.blog.blogai.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Models {
    /**
    * 按照模型的类型进行分类
    * */
    private Map<String, List<Model>> modelResult;


    @Data
    public static class Model {
        private Long id;
        private String modelName;
        private String modelCode;
        private String modelType;
    }
}
