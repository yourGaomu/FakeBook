package com.zhangzc.bookuserapi.Pojo.Dto.Req;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUsersByIdsReqDTO {


    private List<Long> ids;

}
