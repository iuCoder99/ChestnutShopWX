package com.app.uni_app.service.impl;


import com.app.uni_app.common.constant.CaffeineConstant;
import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.PageResult;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.util.CaffeineUtils;
import com.app.uni_app.common.util.SessionUtils;
import com.app.uni_app.mapper.ProductMapper;
import com.app.uni_app.pojo.emums.CommonStatus;
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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

    @Resource
    private SessionUtils sessionUtils;

    @Resource
    private CaffeineUtils caffeineUtils;

    private static final String PRODUCT_LIST = "productList";
    private static final String END_PRODUCT_ID = "endProductId";

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

    /**
     * 二级分类页面的商品列表滚动查询
     *
     * @param categoryId
     * @param beginProductId
     * @return
     */
    @Override
    public Result getCategoryProductList(String categoryId, String beginProductId) {
        List<Product> productList;
        if (StringUtils.equals(beginProductId, Integer.toString(DataConstant.ZERO_INT))) {
            productList = lambdaQuery().eq(Product::getCategoryId, categoryId)
                    .eq(Product::getStatus, CommonStatus.ACTIVE.getNumber()).orderByDesc(Product::getId)
                    .last("LIMIT " + DataConstant.PRODUCT_SCROLL_QUERY_NUMBER).list();
        } else {
            productList = lambdaQuery().eq(Product::getCategoryId, categoryId)
                    .eq(Product::getStatus, CommonStatus.ACTIVE.getNumber())
                    .lt(Product::getId, beginProductId).orderByDesc(Product::getId)
                    .last("LIMIT " + DataConstant.PRODUCT_SCROLL_QUERY_NUMBER).list();
        }
        if (CollectionUtils.isEmpty(productList)) {
            return Result.success();
        }
        List<SimpleProductVO> simpleProductVOs = productList.stream()
                .map(product -> copyMapper.productToSimpleProductVO(product)).toList();
        String endProductId = simpleProductVOs.get(simpleProductVOs.size() - 1).getId().toString();
        HashMap<String, Object> resultMap = new HashMap<>(2);
        resultMap.put(PRODUCT_LIST, simpleProductVOs);
        resultMap.put(END_PRODUCT_ID, endProductId);
        return Result.success(resultMap);
    }

    /**
     * 滚动查询的商品列表
     * @return
     */
    @Override
    public Result getSimpleProductByScrollQuery() {
        HashSet<Long> loadedIdSet = sessionUtils.getLoadedIdSet();
        Long scrollLoadedEndId = sessionUtils.getScrollLoadedEndId();
        Map<String, Long> maxAndMinProductIdInData = caffeineUtils.getMaxAndMinProductIdInData();
        Long maxProductIdInDataOrDefault = maxAndMinProductIdInData.getOrDefault(CaffeineConstant.MAP_KEY_MAX_PRODUCT_ID_IN_DATA, DataConstant.ZERO_LONG);
        Long minProductIdInDataOrDefault = maxAndMinProductIdInData.getOrDefault(CaffeineConstant.MAP_KEY_MIN_PRODUCT_ID_IN_DATA, DataConstant.ZERO_LONG);
        if (scrollLoadedEndId.equals(DataConstant.ZERO_LONG)) {
            if (maxProductIdInDataOrDefault.equals(DataConstant.ZERO_LONG)){
                return Result.success(CollectionUtils.emptyCollection());
            }
            long bound = Math.round(maxProductIdInDataOrDefault * DataConstant.QUERY_SECURITY_NUMBER);
            bound = Math.max(bound, 2);
            scrollLoadedEndId = ThreadLocalRandom.current().nextLong(1, bound);
        }
        List<Product> productList = lambdaQuery().lt(Product::getId, scrollLoadedEndId).orderByDesc(Product::getId).last("LIMIT " + DataConstant.PRODUCT_SCROLL_QUERY_NUMBER).list();
        if (productList.isEmpty()) {
            return Result.success(CollectionUtils.emptyCollection());
        }
        List<Product> products = productList.stream().filter(product -> !loadedIdSet.contains(product.getId())).toList();
        scrollLoadedEndId = productList.get(productList.size() - DataConstant.ONE_INT).getId();
        Set<Long> queryProductIds = products.stream().map(Product::getId).collect(Collectors.toSet());
        loadedIdSet.addAll(queryProductIds);
        sessionUtils.setLoadedIdSet(loadedIdSet);
        sessionUtils.setScrollLoadedEndId(scrollLoadedEndId);
        List<SimpleProductVO> simpleProductVOS = productList.stream().map(product -> copyMapper.productToSimpleProductVO(product)).collect(Collectors.toList());
        Collections.shuffle(simpleProductVOS);
        if (scrollLoadedEndId.equals(minProductIdInDataOrDefault)){
            sessionUtils.removeLoadedIdSet();
            sessionUtils.removeScrollLoadedEndId();
        }
        return Result.success(simpleProductVOS);
    }
}



