package com.app.uni_app.infrastructure.redis.service;

import com.app.uni_app.infrastructure.es.document.ProductDocument;

import java.util.List;

public interface ProductRedisCacheService {

    /**
     * 获取热门商品
     * @return 商品文档列表
     */
    List<ProductDocument> getHotProduct();


    /**
     * 获取热门商品 id 列表
     * @return 热门商品 id 列表
     */
    List<Long> getHotProductIdList();

    /**
     * 获取指定热门商品
     * @param productId 商品id
     * @return 商品文档
     */
    ProductDocument getHotProduct(Long productId);


    /**
     * 获取es最大商品id
     * @return 最大商品id
     */
    Long getMaxProductId();


    /**
     * 初始化最大商品id es -> redis
     */
    void initMaxProductId();
 }
