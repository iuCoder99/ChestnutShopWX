package com.app.uni_app.infrastructure.redis.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:redisKeyTtl.yml" , encoding = "UTF-8", factory = YamlPropertySourceFactory.class)
public class RedisKeyTtlConfig {
}
