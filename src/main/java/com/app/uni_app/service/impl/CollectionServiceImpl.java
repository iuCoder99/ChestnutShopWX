package com.app.uni_app.service.impl;

import com.app.uni_app.aop.annotation.business.RemoveProductCollectionRedisCacheAnnotation;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.result.SimpleCursorCommonEntity;
import com.app.uni_app.common.result.SimpleCursorCommonResult;
import com.app.uni_app.common.util.DateUtils;
import com.app.uni_app.mapper.CollectionMapper;
import com.app.uni_app.pojo.entity.ProductCollection;
import com.app.uni_app.pojo.vo.SimpleProductVO;
import com.app.uni_app.service.CollectionService;
import com.app.uni_app.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, ProductCollection> implements CollectionService {

    @Lazy
    @Resource
    private ProductService productService;


    /**
     * 新增收藏
     * @param productId
     * @return
     */
    @Override
    @RemoveProductCollectionRedisCacheAnnotation
    public Result addCollection(String productId) {
        if (StringUtils.isBlank(productId)) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        String userId = BaseContext.getUserId();
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
    @RemoveProductCollectionRedisCacheAnnotation
    public Result deleteCollection(String productIds) {
        if (StringUtils.isBlank(productIds)) {
            return Result.error(MessageConstant.CONTENT_NOT_EXIST_ERROR);
        }
        String userId = BaseContext.getUserId();
        List<String> productIdList = Arrays.stream(StringUtils.split(productIds, ",")).toList();
        LambdaQueryWrapper<ProductCollection> lambdaQueryWrapper = new LambdaQueryWrapper<ProductCollection>().eq(ProductCollection::getUserId, userId).in(ProductCollection::getProductId, productIdList);
        boolean isSuccess = remove(lambdaQueryWrapper);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(productIdList.size());
    }

    /**
     * 获取用户收藏商品列表
     * @param simpleCursorCommonEntity 简单游标查询参数实体
     * @return 简单商品封装实体
     */
    @Override
    public Result<SimpleCursorCommonResult> getCollectionList(SimpleCursorCommonEntity simpleCursorCommonEntity) {
        String userId = BaseContext.getUserId();
        String sortValue = simpleCursorCommonEntity.getSortValue();
        Integer querySize = simpleCursorCommonEntity.getQuerySize();
        Long sortId = simpleCursorCommonEntity.getSortId();
        boolean isEnd = false;

        LambdaQueryChainWrapper<ProductCollection> wrapper = lambdaQuery()
                .eq(ProductCollection::getUserId, userId)
                .orderByDesc(ProductCollection::getCreateTime, ProductCollection::getId)
                .last("LIMIT " + querySize);

        if (StringUtils.isNotBlank(sortValue) && Objects.nonNull(sortId)) {
            LocalDateTime beginTime = DateUtils.parseToLocalDateTime(sortValue);
            wrapper.apply("(create_time, id) < ({0}, {1})", beginTime, sortId);
        }


        List<ProductCollection> productCollectionList = wrapper.list();
        // 数据查尽
        if (productCollectionList.isEmpty()) {
            SimpleCursorCommonResult simpleCursorCommonResult = SimpleCursorCommonResult.builder()
                    .isEnd(true)
                    .list(Collections.emptyList())
                    .build();
            return Result.success(simpleCursorCommonResult);
        }

        if (productCollectionList.size() < querySize) {
            isEnd = true;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < productCollectionList.size(); i++) {
            if (i == productCollectionList.size() - 1) {
                stringBuilder.append(productCollectionList.get(i).getProductId());
                break;
            }
            stringBuilder.append(productCollectionList.get(i).getProductId()).append(",");
        }
        List<SimpleProductVO> simpleProductVOList = productService.getBriefProduct(stringBuilder.toString()).getData();
        if (simpleProductVOList.isEmpty()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        ProductCollection productCollectionLast = productCollectionList.get(productCollectionList.size() - 1);
        SimpleCursorCommonEntity simpleCursorCommonEntityResult = SimpleCursorCommonEntity.builder()
                .sortId(productCollectionLast.getId())
                .sortValue(DateUtils.formatLocalDateTime(productCollectionLast.getCreateTime()))
                .querySize(querySize)
                .build();
        SimpleCursorCommonResult result = SimpleCursorCommonResult.builder()
                .isEnd(isEnd)
                .simpleCursorCommonEntity(simpleCursorCommonEntityResult)
                .list(simpleProductVOList)
                .build();
        return Result.success(result);

    }


}
