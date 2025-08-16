package com.zhangzc.bookuserbiz.rpc;


import com.zhangzc.bookcommon.Utils.R;
import com.zhangzc.fakebookossapi.Api.FileFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
@RequiredArgsConstructor
public class OssRpcService {

    private final FileFeignApi fileFeignApi;

    public String uploadFile(MultipartFile file) {

        // 调用对象存储服务上传文件
        R response = fileFeignApi.uploadFile(file);

        if (!response.isSuccess()) {
            return null;
        }

        // 返回图片访问链接
        return (String) response.getData();
    }
}
