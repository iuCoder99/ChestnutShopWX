package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.entity.ProductSearchKeyword;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface ProductSearchKeywordService extends IService<ProductSearchKeyword> {
    Result getProductSearchKeywordListUser();

    Result getProductSearchKeywordListAdmin();

    Result updateProductSearchListAdmin(@NotNull List<ProductSearchKeyword> productSearchKeywordList);
}

