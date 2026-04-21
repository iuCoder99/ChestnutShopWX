package com.app.uni_app.infrastructure.redis.generator;

public class RedisKeyBucketGenerator {

    private static final String BUCKET_PREFIX = "bucket:";

    public static String generate(String key) {
        return BUCKET_PREFIX + key;
    }
}
