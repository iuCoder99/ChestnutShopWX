package com.app.uni_app.infrastructure.es.service.impl;

import com.app.uni_app.aop.annotation.common.ParamCheckAnnotation;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.repository.ProductEsRepository;
import com.app.uni_app.infrastructure.es.service.ProductDocumentService;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.pojo.emums.CommonSortTypeEnum;
import com.app.uni_app.pojo.emums.ProductSortTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductDocumentServiceImpl implements ProductDocumentService {

    private final ProductEsRepository productEsRepository;

    @Override
    public ProductDocument getProductDocumentById(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        return productEsRepository.getById(id);
    }

    @Override
    public List<ProductDocument> getProductDocumentById(Long id, Long... ids) {
        return productEsRepository.getById(id, ids);
    }

    @Override
    public List<ProductDocument> getProductDocumentByIdList(List<Long> idList) {
        if (Objects.isNull(idList) || idList.isEmpty()) {
            return Collections.emptyList();
        }
        return productEsRepository.getByIdList(idList);
    }


    @Override
    public List<ProductDocument> getProductDocumentByProductNameKeyword(String productNameKeyword) {
        if (StringUtils.isBlank(productNameKeyword)) {
            return Collections.emptyList();
        }
        return productEsRepository.searchByName(productNameKeyword);
    }

    @Override
    public List<ProductDocument> getProductDocumentByProductNameKeyword(String productNameKeyword, Integer limit) {
        if (StringUtils.isBlank(productNameKeyword) || Objects.isNull(limit)) {
            return Collections.emptyList();
        }
        return productEsRepository.searchByName(productNameKeyword, limit);

    }


    @Override
    public Long getMaxProductDocumentId() {
      return productEsRepository.getMaxId();
    }

    @Override
    public void saveProductDocument(ProductDocument productDocument) {
        productEsRepository.save(productDocument);
    }

    @Override
    public void batchSaveProductDocument(List<ProductDocument> productDocumentList) {
        productEsRepository.batchSave(productDocumentList);
    }

    @Override
    @ParamCheckAnnotation
    public List<ProductDocument> searchLimitAfterProductId(Integer limit, Long productId) {
       return productEsRepository.searchLimitAfterId(limit,productId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductDocument> searchByCursorByCategoryId(Integer limit, ProductSortTypeEnum productSortTypeEnum, String sortValue, Long productId, Long categoryId, Boolean isFirstCategoryId) {
        if (Objects.isNull(isFirstCategoryId)) {
            throw new RuntimeException("是否为一级分类id isFirstCategoryId, 传参为 null");
        }
        //一级分类查询
        if (isFirstCategoryId) {
            String key = RedisKeyGenerator.categoryTreeKey();
            String hashKey = RedisKeyGenerator.categoryTreeHashKey(categoryId);
            List<Long> secondCategoryIdList = RedisConnector.getHashField(key, hashKey, ArrayList.class);
            //首次一级分类查询
            if (Objects.isNull(sortValue) || Objects.isNull(productId)) {
                return productEsRepository.searchLimitByProductSortTypeAndCategoryIdList(productSortTypeEnum, secondCategoryIdList, limit);
            }
            //一级分类游标查询
            return productEsRepository.searchCursorByProductSortTypeAndCategoryIdList(productSortTypeEnum, secondCategoryIdList, limit, sortValue, productId);
        }
        //首次二级分类查询
        if (Objects.isNull(sortValue) || Objects.isNull(productId)) {
            return productEsRepository.searchLimitByProductSortTypeAndCategoryId(productSortTypeEnum, categoryId, limit);
        }
        //二级分类游标查询
        return productEsRepository.searchCursorByProductSortTypeAndCategoryId(productSortTypeEnum, categoryId, limit, sortValue, productId);
    }

    @Override
    public List<ProductDocument> searchByCursorByName(Integer limit, ProductSortTypeEnum productSortTypeEnum, String sortValue, Long productId, String keyword) {
        //首次游标查询
        if (Objects.isNull(sortValue) || Objects.isNull(productId)) {
            return productEsRepository.searchLimitByProductSortTypeAndProductName(productSortTypeEnum, keyword, limit);
        }
        //游标查询
        return productEsRepository.searchCursorByProductSortTypeAndProductName(productSortTypeEnum, keyword, limit, sortValue, productId);
    }

    @Override
    public List<ProductDocument> searchLimitHotProductDocument(Integer limit) {
       return productEsRepository.searchLimitOrderByField(limit,ProductDocument.Fields.salesCount, CommonSortTypeEnum.DESC);
    }
}
