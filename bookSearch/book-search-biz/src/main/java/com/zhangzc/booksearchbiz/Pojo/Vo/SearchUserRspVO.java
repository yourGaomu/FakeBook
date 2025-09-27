package com.zhangzc.booksearchbiz.Pojo.Vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.easyes.annotation.HighLight;
import org.dromara.easyes.annotation.IndexId;
import org.dromara.easyes.annotation.IndexName;
import org.dromara.easyes.annotation.rely.IdType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@IndexName(value = "searchuserinfo")
public class SearchUserRspVO {

    /**
     * 用户ID
     */
    @IndexId(type = IdType.CUSTOMIZE)
    private Long userId;

    /**
     * 昵称
     */
    @HighLight
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 小哈书ID
     */
    private String xiaohashuId;

    /**
     * 笔记发布总数
     */
    private Integer noteTotal;

    /**
     * 粉丝总数
     */
    private Integer fansTotal;

}

