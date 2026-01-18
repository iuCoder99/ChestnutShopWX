package com.app.uni_app.service.impl;


import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.PageResult;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.mapper.ProductMapper;
import com.app.uni_app.pojo.emums.ProductSortType;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.pojo.entity.ProductSpec;
import com.app.uni_app.pojo.vo.ProductSpecVO;
import com.app.uni_app.pojo.vo.SimpleProductVO;
import com.app.uni_app.service.ProductService;
import com.app.uni_app.service.ProductSpecService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author 20589
 * @description 针对表【product(商品表)】的数据库操作Service实现
 * @createDate 2025-12-23 19:32:49
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {
    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductSpecService productSpecService;

    @Resource
    private CopyMapper copyMapper;

    /**
     * 获取热门商品
     * 按照销量进行排名
     *
     * @return
     */
    @Override
    public Result getHotProduct(Integer limit) {
        List<Product> hotProducts = productMapper.selectOrderByDescSalesCountLimit(limit);
        List<SimpleProductVO> simpleProductVOs = hotProducts.stream()
                .map(hotProduct -> copyMapper.productToSimpleProductVO(hotProduct)).toList();
        return Result.success(simpleProductVOs);
    }

    /**
     * 获取列表商品简单介绍
     *
     * @param productIds
     * @return
     */
    @Override
    public Result getBriefProduct(String productIds) {
        if (StringUtils.isBlank(productIds)) {
            return Result.success(CollectionUtils.emptyCollection());
        }
        List<String> productIdsList = Arrays.stream(StringUtils.split(productIds, ",")).toList();
        List<Product> list = productMapper.getBriefProduct(productIdsList);
        List<SimpleProductVO> simpleProductVOS = list.stream()
                .map(product -> copyMapper.productToSimpleProductVO(product)).toList();
        return Result.success(simpleProductVOS);

    }

    /**
     * 获取商品详情
     *
     * @param productId
     * @return
     */
    @Override
    public Result getProductDetail(String productId, String userId) {
        if (StringUtils.isBlank(productId)) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        if (StringUtils.isBlank(userId)) {
            userId = "-1";
        }
        Product product = productMapper.selectByProductId(productId, userId);
        if (Objects.isNull(product)) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        if (!StringUtils.equals(product.getIsCollection().toString(), "0")) {
            product.setIsCollection(1L);
        }
        return Result.success(product);
    }

    /**
     * 获取商品列表
     *
     * @param pageNum
     * @param pageSize
     * @param categoryId
     * @return
     */
    @Override
    public Result getProductList(Integer pageNum, Integer pageSize, String categoryId) {
        IPage<Product> page = productMapper.selectByCategoryIdPage(new Page<>(pageNum, pageSize), categoryId);
        PageResult pageResult = PageResult.builder().list(page.getRecords()).total(page.getTotal())
                .pageNum(pageNum).pageSize(pageSize).build();
        return Result.success(pageResult);
    }

    /**
     * 查询商品列表
     *
     * @param pageNum
     * @param pageSize
     * @param firstCategoryId
     * @param secondCategoryId
     * @param sortType
     * @param keyword
     * @return
     */
    @Override
    public Result searchProductList(Integer pageNum, Integer pageSize, String firstCategoryId, String secondCategoryId, String sortType, String keyword) {
        String dbValue = ProductSortType.getByValue(sortType).getDbValue();
        IPage<Product> page;
        //只有一级分类
        if (StringUtils.isNotBlank(firstCategoryId) && StringUtils.isBlank(secondCategoryId)) {
            page = productMapper.selectByFirstCategoryIdAndKeywordPage(new Page<>(pageNum, pageSize), firstCategoryId, dbValue, keyword);
        }
        //一级+二级 或 空
        else {
            page = productMapper.selectBySecondCategoryIdAndKeywordPage(new Page<>(pageNum, pageSize), firstCategoryId, secondCategoryId, dbValue, keyword);
        }
        PageResult pageResult = PageResult.builder().list(page.getRecords()).total(page.getTotal())
                .pageNum(pageNum).pageSize(pageSize).build();
        return Result.success(pageResult);
    }

    /**
     * 获取相关商品(通过categoryId建立联系)
     *
     * @return
     */
    @Override
    public Result getProductRelated(String productId, Integer limit) {
        List<Product> products = productMapper.selectRelatedByCategoryId(productId, limit);
        return Result.success(products);
    }

    /**
     * 获取商品规格价格
     *
     * @param productId
     * @param specId
     * @return
     */
    @Override
    public Result getProductSpecPrice(String productId, String specId) {
        ProductSpec productSpec = productSpecService.lambdaQuery().eq(ProductSpec::getProductId, productId)
                .eq(ProductSpec::getId, specId).one();
        ProductSpecVO productSpecVO = copyMapper.productSpecToProductSpecVO(productSpec);
        return Result.success(productSpecVO);
    }
}




