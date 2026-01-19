package com.app.uni_app.service;


import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotBlank;

/**
 * @author 20589
 * @description 针对表【product(商品表)】的数据库操作Service
 * @createDate 2025-12-23 19:32:49
 */
public interface ProductService extends IService<Product> {

    Result getHotProduct(Integer limit);

    Result getProductDetail(String productId,String userId);

    Result getProductList(Integer pageNum, Integer pageSize, String categoryId);

    Result searchProductList(Integer pageNum, Integer pageSize, String firstCategoryId, String secondCategoryId, String sortType, String keyword);

    Result getProductRelated(String productId, Integer limit);

    Result getProductSpecPrice(String productId, String specId);

    Result getBriefProduct(String productIds);

    Result getCategoryProductList(@NotBlank String categoryId, String beginProductId);

    Result getSimpleProductByScrollQuery();
}

