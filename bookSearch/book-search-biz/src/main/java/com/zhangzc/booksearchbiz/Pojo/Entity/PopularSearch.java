package com.zhangzc.booksearchbiz.Pojo.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("popular_search")
public class PopularSearch implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private Long searchCount;
    private Double heatScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
