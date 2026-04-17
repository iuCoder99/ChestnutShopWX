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
     * 根据分类id进行查询
     * @param limit 查询数
     * @param productSortTypeEnum 商品排序枚举
     * @param fieldValue 排序字段值
     * @param productId 商品 id
     * @param categoryId 分类 id (一级或二级分类 id )
     * @param isFirstCategoryId 是否为一级分类 id
     * @return 查询文档列表
     */
    List<ProductDocument> searchByCursorByCategoryId(Integer limit, ProductSortTypeEnum productSortTypeEnum, String sortValue, Long productId, Long categoryId, Boolean isFirstCategoryId);




}
