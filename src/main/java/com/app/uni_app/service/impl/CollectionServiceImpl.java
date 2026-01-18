package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.PageResult;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.mapper.CollectionMapper;
import com.app.uni_app.pojo.entity.ProductCollection;
import com.app.uni_app.service.CollectionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, ProductCollection> implements CollectionService {

    /**
     * 新增收藏
     * @param productId
     * @return
     */
    @Override
    public Result addCollection(String productId) {
        if (StringUtils.isBlank(productId)) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        String userId = BaseContext.getUserInfo().getId();
        ProductCollection productCollection = new ProductCollection();
        productCollection.setUserId(Long.valueOf(userId)).setProductId(Long.valueOf(productId));
        boolean isSuccess = save(productCollection);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success();
    }

    /**
     * 删除收藏
     * @param productIds
     * @return
     */
    @Override
    public Result deleteCollection(String productIds) {
        if (StringUtils.isBlank(productIds)) {
            return Result.error(MessageConstant.CONTENT_NOT_EXIST_ERROR);
        }
        String userId = BaseContext.getUserInfo().getId();
        List<String> productIdList = Arrays.stream(StringUtils.split(productIds, ",")).toList();
        LambdaQueryWrapper<ProductCollection> lambdaQueryWrapper = new LambdaQueryWrapper<ProductCollection>().eq(ProductCollection::getUserId, userId).in(ProductCollection::getProductId, productIdList);
        boolean isSuccess = remove(lambdaQueryWrapper);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(productIdList.size());
    }

    /**
     * 获取收藏列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Result getCollectionList(Integer pageNum, Integer pageSize) {
        String userId = BaseContext.getUserInfo().getId();
        Page<ProductCollection> page = lambdaQuery().eq(ProductCollection::getUserId, userId).page(new Page<>(pageNum, pageSize));
        return Result.success(PageResult.builder().list(page.getRecords()).pageSize(pageSize).pageNum(pageNum).total(page.getTotal()).build());
    }
}
