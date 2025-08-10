package com.zhangzc.fakebookossbiz.Controller;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.fakebookossbiz.Service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: zhangzc
 * @date: 2024/4/4 13:22
 * @version: v1.0.0
 * @description: 文件
 **/
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {


    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R uploadFile(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return fileService.uploadFile(file);
    }

}
