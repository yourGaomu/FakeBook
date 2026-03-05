package com.zhangzc.blog.blogai.Controller;

import com.zhangzc.miniospringbootstart.utills.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/upload")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final MinioUtil minioUtil;

    @PostMapping("/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 调用 MinioUtil 的 uploadTemporaryFile 方法，传入项目前缀 "shareThing"
            String url = minioUtil.uploadTemporaryFile(file, "shareThing");
            result.put("code", 200);
            result.put("data", url);
            result.put("msg", "上传成功");
            log.info("Image uploaded successfully: {}", url);
        } catch (Exception e) {
            log.error("Image upload failed", e);
            result.put("code", 500);
            result.put("msg", "上传失败: " + e.getMessage());
        }
        return result;
    }
}
