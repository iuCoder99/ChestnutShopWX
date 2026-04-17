package com.app.uni_app.infrastructure.es.repository;

import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.pojo.emums.ProductSortTypeEnum;

import java.util.List;

/**
 * 商品交互类
 */
public interface ProductEsRepository {

    /**
     * 根据指定 id 获取商品文档
     * @param id 商品 id
     * @return 商品文档
     */
    ProductDocument getById(Long id);

    /**
     * 根据 id 列表获取商品文档
     * @param id 商品 id
     * @param ids 商品 id 不定数量
     * @return 商品文档列表
     */
    List<ProductDocument> getById(Long id, Long... ids);

    /**
     * 根据 id 列表获取商品文档列表
     * @param idList 商品 id 列表
     * @return 商品文档列表
     */
    List<ProductDocument> getByIdList(List<Long> idList);


    /**
     * 保存单个商品文档
     * @param document 要保存的文档
     */
    void save(ProductDocument document);

    /**
     * 批量保存商品文档
     * @param documents 要批量保存的文档
     */
    void batchSave(List<ProductDocument> documents);

    /**
     * 根据 id 删除指定商品文档
     * @param id 商品文档 id
     */
    void deleteById(Long id);

    /**
     * 根据商品文档名进行查询
     * @param name 商品文档名
     * @return 查询商品文档列表
     */
    List<ProductDocument> searchByName(String name);

    /**
     * 根据商品文档名进行查询
     * @param name 商品文档名
     * @param limit 查询数量
     * @return 查询商品文档列表
     */
    List<ProductDocument> searchByName(String name, Integer limit);

    /**
     * 根据查询种类和分类 id 首次进行游标查询 (无需开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryId 查询商品分类 id
     * @param limit 查询数
     * @return 商品文档列表
     */
    List<ProductDocument> searchLimitByProductSortTypeAndCategoryId(ProductSortTypeEnum productSortTypeEnum, Long categoryId, Integer limit);


    /**
     * 根据查询种类和分类 id 首次进行游标查询 (需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryId 查询商品分类 id
     * @param limit 查询数
     * @param sortValue 开始游标值
     * @param productId 开始商品 id
     * @return 商品文档列表
     */
    List<ProductDocument> searchCursorByProductSortTypeAndCategoryId(ProductSortTypeEnum productSortTypeEnum, Long categoryId, Integer limit , String sortValue, Long productId);


    /**
     * 根据查询种类和分类 id 首次进行游标查询 (需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryIdList 分类 id 集合
     * @param limit 查询数
     * @return 商品文档列表
     */
     List<ProductDocument> searchLimitByProductSortTypeAndCategoryIdList(ProductSortTypeEnum productSortTypeEnum, List<Long> categoryIdList, Integer limit);

    /**
     * 根据查询种类和分类 id 首次进行游标查询 (需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryIdList 分类 id 集合
     * @param limit 查询数
     * @param sortValue 开始游标值
     * @param productId 开始商品 id
     * @return 商品文档列表
     */
    List<ProductDocument> searchCursorByProductSortTypeAndCategoryIdList(ProductSortTypeEnum productSortTypeEnum, List<Long> categoryIdList, Integer limit, String sortValue, Long productId);




}
