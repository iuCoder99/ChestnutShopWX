package com.app.uni_app.infrastructure.es.service;

import com.app.uni_app.infrastructure.es.document.ProductDocument;

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
     * 保存单个商品文档
     * @param productDocument 商品文档
     */
    void saveProductDocument(ProductDocument productDocument);

    /**
     * 批量保存商品文档列表
     * @param productDocumentList 商品文档列表
     */
    void batchSaveProductDocument(List<ProductDocument> productDocumentList);

}
