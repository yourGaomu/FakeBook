package com.zhangzc.booksearchbiz.Pojo.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
@TableName("search_history")
public class SearchHistory implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String keyword;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @TableLogic(value = "0", delval = "1")
    private Boolean isDelete;
}
