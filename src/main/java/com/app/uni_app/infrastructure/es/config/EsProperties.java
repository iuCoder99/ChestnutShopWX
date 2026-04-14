package com.app.uni_app.infrastructure.es.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Spring 专用 ES 配置绑定
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.elasticsearch")
public class EsProperties {
    /**
     * ES 连接地址
     */
    private String uris;
}