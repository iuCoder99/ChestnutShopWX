package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.entity.ProductCollection;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotBlank;

public interface CollectionService extends IService<ProductCollection> {
    Result addCollection(String productId);

    Result deleteCollection(@NotBlank String productIds);

    Result getCollectionList(Integer pageNum, Integer pageSize);
}
