package com.app.uni_app.infrastructure.es.service;

import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.pojo.emums.ProductSortTypeEnum;

import java.util.List;

public interface ProductDocumentService {

    /**
     * 通过指定 id 获取商品文档
     * @param id 商品 id
     * @return 商品文档
     */
    ProductDocument getProductDocumentById(Long id);


    /**
     * 通过指定 ids 批量获取商品文档
     * @param id 商品 id
     * @param ids 不定量 商品 id
     * @return 商品文档列表
     */
    List<ProductDocument> getProductDocumentById(Long id, Long... ids);


    /**
     * 通过 id 列表批量获取商品文档
     * @param idList id 列表
     * @return 商品文档列表
     */
    List<ProductDocument> getProductDocumentByIdList(List<Long> idList);


    /**
     * 根据商品名关键词查询
     * @param productNameKeyword 商品名关键词
     * @return 商品文档列表
     */
    List<ProductDocument> getProductDocumentByProductNameKeyword(String productNameKeyword);


    /**
     * 根据商品名关键词查询
     * @param productNameKeyword 商品名关键词
     * @param limit 查询数量
     * @return 商品文档列表
     */
    List<ProductDocument> getProductDocumentByProductNameKeyword(String productNameKeyword,Integer limit);


    /**
     * 获取最大商品文档 id
     * @return 最大商品文档 id
     */
    Long getMaxProductDocumentId();

    /**
     * 保存单个商品文档
     * @param productDocument 商品文档
     */
    void saveProductDocument(ProductDocument productDocument);

    /**
     * 批量保存商品文档列表
     * @param productDocumentList 商品文档列表
     */
    void batchSaveProductDocument(List<ProductDocument> productDocumentList);

    /**
     * 查询指定商品id之后的指定数量的商品文档
     * @param limit 查询数量
     * @param productId 商品 id
     * @return  商品文档列表
     */
    List<ProductDocument> searchLimitAfterProductId(Integer limit , Long productId);

    /**
     * 根据分类id进行查询
     * @param limit 查询数
     * @param productSortTypeEnum 商品排序枚举
     * @param productId 商品 id
     * @param categoryId 分类 id (一级或二级分类 id )
     * @param isFirstCategoryId 是否为一级分类 id
     * @return 查询文档列表
     */
    List<ProductDocument> searchByCursorByCategoryId(Integer limit, ProductSortTypeEnum productSortTypeEnum, String sortValue, Long productId, Long categoryId, Boolean isFirstCategoryId);


    /**
     * 根据商品关键词进行查询
     * @param limit 查询数
     * @param productSortTypeEnum 商品排序枚举
     * @param sortValue 游标开始值
     * @param productId 商品 id
     * @param keyword 关键词
     * @return 查询文档列表
     */
    List<ProductDocument> searchByCursorByName(Integer limit, ProductSortTypeEnum productSortTypeEnum ,String sortValue, Long productId, String keyword);


    /**
     * 查询指定数量的热门商品
     * @param limit 查询数量
     * @return 热门商品文档列表
     */
     List<ProductDocument> searchLimitHotProductDocument(Integer limit);




}
