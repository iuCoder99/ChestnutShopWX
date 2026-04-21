package com.app.uni_app.infrastructure.redis.properties.config;


import com.app.uni_app.infrastructure.redis.config.YamlPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:redisCache.yml" , encoding = "UTF-8", factory = YamlPropertySourceFactory.class)
public class RedisCacheConfig {
}
