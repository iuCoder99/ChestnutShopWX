package com.app.uni_app.controller.admin;


import com.app.uni_app.common.result.Result;
import com.app.uni_app.service.FactoryInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/about/us")
@Tag(name = "工厂信息管理")
public class FactoryInfoController {


    @Resource
    private FactoryInfoService factoryInfoService;

    @GetMapping("/introduce")
    @Operation(summary = "查询单条工厂信息,现在用于个人中心的关于我们")
    public Result<Object> getFactoryInfo(){
       return factoryInfoService.getFactoryInfo();
    }
}
