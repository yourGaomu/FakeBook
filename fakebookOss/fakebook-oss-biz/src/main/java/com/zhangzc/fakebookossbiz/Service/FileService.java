package com.zhangzc.fakebookossbiz.Service;

import com.zhangzc.bookcommon.Utils.R;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    R uploadFile(MultipartFile file) throws Exception;
}
