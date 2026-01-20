package com.app.uni_app.common.utils;

import com.app.uni_app.common.constant.CaffeineConstant;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CaffeineUtil {

    @Resource
    private LoadingCache<String, List<String>> hotProductSearchKeywordCache;


    public List<String> getHotProductSearchKeyword(String cacheKey) {
        return hotProductSearchKeywordCache.get(cacheKey);
    }

    public void invalidateHotProductSearchKeywordCache() {
        hotProductSearchKeywordCache.invalidate(CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD);
    }



}
