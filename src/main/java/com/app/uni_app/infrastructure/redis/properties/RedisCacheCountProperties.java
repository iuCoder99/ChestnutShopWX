package com.app.uni_app.infrastructure.redis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redis.cache.count")
@Data
public class RedisCacheCountProperties {

    private int hotProductCacheSize;
}
