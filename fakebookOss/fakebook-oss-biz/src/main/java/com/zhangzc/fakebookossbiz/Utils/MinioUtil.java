package com.zhangzc.fakebookossbiz.Utils;


import com.zhangzc.bookcommon.Exceptions.BizException;
import com.zhangzc.fakebookossbiz.Config.Properties.MinioProperties;
import com.zhangzc.fakebookossbiz.Exceptions.FileUpDownException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Component
public class MinioUtil {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String uploadFile(MultipartFile file) throws Exception {
        // 判断文件是否为空
        if (file == null || file.getSize() == 0) {
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }
        // 文件的 Content-Type
        String contentType = file.getContentType();

        // 文件的原始名称
        String originalFileName = file.getOriginalFilename();

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 时间戳格式（用于文件名，移除非法字符）
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(timestampFormatter);

        // 日期文件夹格式（用于组织文件）
        DateTimeFormatter dateFolderFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFolder = now.format(dateFolderFormatter);

        // 生成无横线的UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 安全获取文件后缀
        String suffix = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // 构建完整对象名：日期文件夹/时间戳_UUID.后缀
        String objectName = dateFolder + "/" + timestamp + "_" + uuid + suffix;

        log.info("==> 开始上传文件至 Minio, ObjectName: {}", objectName);

        // 上传文件至 Minio
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new BizException(FileUpDownException.FILE_UPLOAD_ERROR);
        }

        // 返回文件的访问链接
        String url = String.format("%s/%s/%s", minioProperties.getEndpoint(), minioProperties.getBucketName(), objectName);
        log.info("==> 上传文件至 Minio 成功，访问路径: {}", url);
        return url;
    }
}
