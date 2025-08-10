package com.zhangzc.fakebookossbiz.Service.Impl;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.fakebookossbiz.Service.FileService;
import com.zhangzc.fakebookossbiz.Utils.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinIOFilServiceImpl implements FileService {
    private final MinioUtil minioUtil;

    @Override
    public R uploadFile(MultipartFile file) throws Exception {
        log.info("==> 开始上传文件至 Minio ...");
        String url = minioUtil.uploadFile(file);
        return R.success(url);
    }
}
