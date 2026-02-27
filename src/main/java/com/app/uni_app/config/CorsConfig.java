package com.app.uni_app.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许跨域凭证
        config.setAllowCredentials(true);

        // 允许的源（前端开发地址）
        config.addAllowedOrigin("http://127.0.0.1:63467"); // 原有小程序地址
        config.addAllowedOrigin("http://localhost:8081");   // H5 端地址
        config.addAllowedOrigin("http://127.0.0.1:8081");   // H5 端地址
        config.addAllowedOriginPattern("*"); // 为了方便 Ngrok 调试，暂时允许所有 Pattern

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有 HTTP 方法
        config.addAllowedMethod("*");

        // 注册配置
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        // 设置优先级最高，确保在 Shiro 过滤器之前执行
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}