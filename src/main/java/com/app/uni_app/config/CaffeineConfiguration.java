package com.app.uni_app.config;

import com.app.uni_app.common.constant.CaffeineConstant;
import com.app.uni_app.pojo.entity.Category;
import com.app.uni_app.service.impl.CategoryServiceImpl;
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

@Configuration
public class CaffeineConfiguration {

    @Resource
    private ProductSearchKeywordServiceImpl productSearchKeywordServiceImpl;

    @Resource
    private CategoryServiceImpl categoryServiceImpl;;

    /**
     * 商品搜索关键词缓存
     * @return
     */
    @Bean
    public LoadingCache<String, List<String>> hotProductSearchKeywordCache() {
        return Caffeine.newBuilder()
                .initialCapacity(1)
                .maximumSize(1)
                // .expireAfterWrite(12, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                           @Override
                           public @Nullable List<String> load(String key) throws IllegalArgumentException {
                               if (StringUtils.equals(key, CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD)) {
                                   return productSearchKeywordServiceImpl.getHotProductSearchKeywordListUser(key);
                               }
                               throw new IllegalArgumentException(CaffeineConstant.CACHE_KEY_NOT_VALID_ERROR);
                           }
                       }
                );
    }

    /**
     *分类树(一级分类,二级分类)缓存
     * @return
     */
    @Bean
    public LoadingCache<String, List<Category>> categoryTreeCache() {
        return Caffeine.newBuilder()
                .initialCapacity(1)
                .maximumSize(1)
                .build(new CacheLoader<>() {
                    @Override
                    public @Nullable List<Category> load(String key) throws IllegalArgumentException {
                        if (StringUtils.equals(key, CaffeineConstant.CACHE_KEY_CATEGORY_TREE)) {
                            return categoryServiceImpl.getCategoryTreeCache();
                        }
                        throw new IllegalArgumentException(CaffeineConstant.CACHE_KEY_NOT_VALID_ERROR);
                    }
                });
    }
}
