package com.app.uni_app.common.util;

import com.app.uni_app.common.constant.CaffeineConstant;
import com.app.uni_app.mapper.ProductMapper;
import com.app.uni_app.pojo.entity.Category;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public  class CaffeineUtils {

    @Resource
    private LoadingCache<String, List<String>> hotProductSearchKeywordCache;

    @Resource
    private LoadingCache<String, List<Category>> categoryTreeCache;

    @Resource
    private Cache<String, Long> maxProductIdInDataCache;

    @Resource
    private ProductMapper productMapper;

    /**
     * 查询热门搜索关键词
     */
    public  List<String> getHotProductSearchKeyword() {
        return hotProductSearchKeywordCache.get(CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD);
    }

    /**
     * 清除热门搜索关键词
     */
    public void invalidateHotProductSearchKeywordCache() {
        hotProductSearchKeywordCache.invalidate(CaffeineConstant.CACHE_KEY_HOT_PRODUCT_SEARCH_KEYWORD);
    }

    /**
     * 查询分类树
     */
    public List<Category> getCategoryTree() {
        return categoryTreeCache.get(CaffeineConstant.CACHE_KEY_CATEGORY_TREE);
    }

    /**
     * 删除分类树
     */
    public void invalidateCategoryTree() {
        categoryTreeCache.invalidate(CaffeineConstant.CACHE_KEY_CATEGORY_TREE);
    }

    /**
     * 获取数据库中最大的商品 id
     */
    public Long getMaxProductIdInData() {
        return maxProductIdInDataCache.get(CaffeineConstant.CACHE_KEY_MAX_PRODUCT_ID_IN_DATA,s->productMapper.getMaxProductIdInData());
    }

    /**
     * 更新数据库中最大的商品 id
     */
    public void updateMaxProductIdInData(){
        Long maxProductIdInData = productMapper.getMaxProductIdInData();
        maxProductIdInDataCache.put(CaffeineConstant.CACHE_KEY_MAX_PRODUCT_ID_IN_DATA,maxProductIdInData);
    }}
