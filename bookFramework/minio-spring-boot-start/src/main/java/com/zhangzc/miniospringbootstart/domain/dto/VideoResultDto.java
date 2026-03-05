package com.zhangzc.miniospringbootstart.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class VideoResultDto {
    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 视频封面地址
     */
    private String coverUrl;
    
    /**
     * 视频宽
     */
    private Integer width;
    
    /**
     * 视频高
     */
    private Integer height;
}
