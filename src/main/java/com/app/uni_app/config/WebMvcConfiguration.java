package com.app.uni_app.config;

import com.app.uni_app.interceptor.JwtTokenUserInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Slf4j
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {
    @Resource
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
//    protected void addInterceptors(InterceptorRegistry registry) {
//        log.info("开始注册自定义拦截器...");
//        registry.addInterceptor(jwtTokenUserInterceptor)
//                .addPathPatterns("/api/**")
//                .excludePathPatterns("/api/user/login/**")
//                .excludePathPatterns("/api/user/create/**")
//                .excludePathPatterns("/api/banner/list")
//                .excludePathPatterns("/api/product/**")
//                .excludePathPatterns("/api/category/**")
//                .excludePathPatterns("/api/notice/**");
//
//
//    }
}
