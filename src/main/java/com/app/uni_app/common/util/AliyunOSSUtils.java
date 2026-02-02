package com.app.uni_app.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.app.uni_app.properties.AliyunOSSProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class AliyunOSSUtils {

    @Resource
    private AliyunOSSProperties aliyunOSSProperties;

    /**
     * 上传文件到阿里云 OSS
     *
     * @param file 待上传的文件
     * @return 文件在 OSS 上的访问路径
     */
    public String upload(MultipartFile file) {
        // 获取上传文件的原始文件名
        String originalFilename = file.getOriginalFilename();
        // 构造唯一的文件名（UUID + 后缀）
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 创建 OSS 实例
        OSS ossClient = new OSSClientBuilder().build(
                aliyunOSSProperties.getEndpoint(),
                aliyunOSSProperties.getAccessKeyId(),
                aliyunOSSProperties.getAccessKeySecret()
        );

        try {
            // 上传文件到指定的 Bucket
            ossClient.putObject(aliyunOSSProperties.getBucketName(), fileName, file.getInputStream());
        } catch (IOException e) {
            log.error("文件上传到阿里云 OSS 失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 构造文件访问路径
        // 格式：https://bucketName.endpoint/fileName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(aliyunOSSProperties.getBucketName())
                .append(".")
                .append(aliyunOSSProperties.getEndpoint())
                .append("/")
                .append(fileName);

        log.info("文件上传成功，访问路径为: {}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
