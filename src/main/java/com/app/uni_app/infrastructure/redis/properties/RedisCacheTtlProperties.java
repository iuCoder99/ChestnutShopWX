package com.app.uni_app.infrastructure.redis.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redis.cache.ttl")
@Data
public class RedisCacheTtlProperties {

    private long productDetailTtl;

    private long productCollectionTtl;

    private long cartTtl;

    private long orderTtl;

    private long productFirstCommentTtl;

    private long productSecondCommentTtl;

    private long productAppendCommentTtl;

    private long productCommentCountTtl;

    private long productCommentUserIdSetTtl;

    private long userProductCommentLikeIdSetTtl;


}
