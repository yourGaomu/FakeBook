package com.zhangzc.fakebookossbiz.Controller;

import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.fakebookossbiz.Service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/oss")
public class FileOssController {

    private final FileService fileService;

    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R uploadFile(@RequestPart(value = "file") MultipartFile file) throws Exception {
        return fileService.uploadFile(file);
    }
}
