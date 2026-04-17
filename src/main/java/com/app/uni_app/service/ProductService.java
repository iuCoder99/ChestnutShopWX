package com.app.uni_app.service;


import com.app.uni_app.common.result.CursorCommonEntity;
import com.app.uni_app.common.result.CursorCommonResult;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.pojo.vo.SimpleProductVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface ProductService extends IService<Product> {

    Result getHotProduct(Integer limit);

    Result getProductDetail(String productId, String userId);

    Result getProductList(Integer pageNum, Integer pageSize, String categoryId);

    Result searchProductList(Integer pageNum, Integer pageSize, String firstCategoryId, String secondCategoryId, String sortType, String keyword);

    List<ProductDocument> getProductRelated(String productName, Integer limit);

    Result getProductSpecPrice(String productId, String specId);

    Result<List<SimpleProductVO>> getBriefProduct(String productIds);

    Result getCategoryProductList(@NotBlank String categoryId, String beginProductId, String sortType);

    Result getSimpleProductByScrollQuery();

    Map<Long, Product> getProductDetailByProductIdSet(Set<Long> productIdSet);

    CursorCommonResult getCategorySimpleProduct(@Valid @NotNull CursorCommonEntity cursorCommonEntity ,Long categoryId , boolean isFirstCategoryId);
}

