package com.zhangzc.bookcommentbiz.Pojo.Vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValueAndFlagVo {
    private Object value;
    private boolean updated;

    public boolean getUpdated() {
        return updated;
    }
}
