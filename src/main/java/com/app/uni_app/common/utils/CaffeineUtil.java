package com.app.uni_app.common.utils;

import com.app.uni_app.common.constant.CaffeineConstant;
import com.app.uni_app.pojo.entity.Category;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CaffeineUtil {

    @Resource
    private LoadingCache<String, List<String>> hotProductSearchKeywordCache;

    @Resource
    private LoadingCache<String,List<Category>>  categoryTreeCache;

    public List<String> getHotProductSearchKeyword() {
        return hotProductSearchKeywordCache.get(CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD);
    }

    public void invalidateHotProductSearchKeywordCache() {
        hotProductSearchKeywordCache.invalidate(CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD);
    }

    public List<Category> getCategoryTree() {
        return categoryTreeCache.get(CaffeineConstant.CACHE_KEY_CATEGORY_TREE);
    }

    public void invalidateCategoryTree() {
        categoryTreeCache.invalidate(CaffeineConstant.CACHE_KEY_CATEGORY_TREE);
    }



}
