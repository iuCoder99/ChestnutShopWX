package com.app.uni_app.config;

import com.app.uni_app.common.constant.CaffeineConstant;
import com.app.uni_app.service.impl.ProductSearchKeywordServiceImpl;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfiguration {

    @Resource
    private ProductSearchKeywordServiceImpl productSearchKeywordServiceImpl;

    @Bean
    public LoadingCache<String, List<String>> hotProductSearchKeywordCache() {
        return Caffeine.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public @Nullable List<String> load(String key) throws Exception {
                        if (StringUtils.equals(key, CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD)) {
                            return productSearchKeywordServiceImpl.getHotProductSearchKeywordListUser(key);
                        }
                        throw new Exception(CaffeineConstant.CACHE_KEY_NOT_VALID_ERROR);
                    }
                });
    }
}
