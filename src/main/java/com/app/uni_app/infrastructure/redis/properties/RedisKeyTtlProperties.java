package com.app.uni_app.infrastructure.redis.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redis.key")
@Data
public class RedisKeyTtlProperties {

    private long productDetailTtl;

    private long productCollectionTtl;

    private long cartTtl;

}
