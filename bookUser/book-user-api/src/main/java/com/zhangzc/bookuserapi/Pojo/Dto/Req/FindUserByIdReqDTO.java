package com.zhangzc.bookuserapi.Pojo.Dto.Req;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByIdReqDTO {

    /**
     * 手机号
     */
    private Long id;

}

