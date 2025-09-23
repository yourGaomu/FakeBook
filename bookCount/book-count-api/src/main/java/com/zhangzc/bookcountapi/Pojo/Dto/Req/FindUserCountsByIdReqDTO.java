package com.zhangzc.bookcountapi.Pojo.Dto.Req;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserCountsByIdReqDTO {

    /**
     * 用户 ID
     */

    private Long userId;

}

