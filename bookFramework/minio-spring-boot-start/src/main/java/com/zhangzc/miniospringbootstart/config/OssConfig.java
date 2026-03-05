package com.zhangzc.miniospringbootstart.config;


import com.zhangzc.miniospringbootstart.utills.MinioUtil;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
public class OssConfig {
    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }


    @Bean
    public MinioUtil minioUtil(MinioClient minioClient) {
        return new MinioUtil(minioClient, minioProperties);
    }

}
