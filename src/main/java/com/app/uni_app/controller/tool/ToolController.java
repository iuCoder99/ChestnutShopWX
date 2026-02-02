package com.app.uni_app.controller.tool;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.util.AliyunOSSUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Tag(name = "工具")
@Slf4j
public class ToolController {

    @Resource
    private AliyunOSSUtils aliyunOSSUtils;

    @PostMapping("/upload/image")
    @Operation(summary = "图片上传")
    public Result<String> upload(MultipartFile file) {
        log.info("图片上传: {}", file.getOriginalFilename());
        String url = aliyunOSSUtils.upload(file);
        return Result.success(url);
    }
}
