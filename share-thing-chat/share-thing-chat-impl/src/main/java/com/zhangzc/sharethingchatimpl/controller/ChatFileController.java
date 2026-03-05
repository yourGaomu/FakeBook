package com.zhangzc.sharethingchatimpl.controller;

import com.zhangzc.miniospringbootstart.domain.dto.VideoResultDto;
import com.zhangzc.miniospringbootstart.utills.MinioUtil;

import com.zhangzc.sharethingchatimpl.utils.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatFileController {

    private final MinioUtil minioUtil;

    /**
     * 上传聊天文件 (图片、语音、视频)
     *
     * @param file 文件
     * @return 文件访问 URL
     */
    @PostMapping("/file/video/upload")
    public R<VideoResultDto> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传文件: {}, 大小: {}", file.getOriginalFilename(), file.getSize());
            String videoUrl = minioUtil.uploadTemporaryFile(file,"fakeBook");
            // 获取视频封面和宽高
            VideoResultDto videoResultDto = minioUtil.getCoverUrlByVideoUrl(videoUrl);
            // 设置视频地址
            videoResultDto.setVideoUrl(videoUrl);
            
            log.info("文件上传成功: {}", videoUrl);
            return R.ok("上传成功", videoResultDto);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.error("上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/file/image/upload")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try{
            String imageUrl = minioUtil.uploadTemporaryFile(file,"fakeBook");
            return R.ok(imageUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
