package com.app.uni_app.service.impl;


import com.app.uni_app.common.constant.CaffeineConstant;
import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.PageResult;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.util.BloomFilterUtils;
import com.app.uni_app.common.util.CaffeineUtils;
import com.app.uni_app.common.util.JacksonUtils;
import com.app.uni_app.common.util.SessionUtils;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.connect.StringRedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.properties.RedisKeyTtlProperties;
import com.app.uni_app.mapper.ProductMapper;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.emums.ProductSortType;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.pojo.entity.ProductCollection;
import com.app.uni_app.pojo.entity.ProductSpec;
import com.app.uni_app.pojo.vo.ProductSpecVO;
import com.app.uni_app.pojo.vo.SimpleProductVO;
import com.app.uni_app.service.CollectionService;
import com.app.uni_app.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 20589
 * @description 针对表【product(商品表)】的数据库操作Service实现
 * @createDate 2025-12-23 19:32:49
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {
    @Resource
    private ProductMapper productMapper;

    @Resource
    private CollectionService collectionService;

    @Resource
    private CopyMapper copyMapper;

    @Resource
    private SessionUtils sessionUtils;

    @Resource
    private CaffeineUtils caffeineUtils;

    @Resource
    private BloomFilterUtils bloomFilterUtils;

    @Resource
    private RedisKeyTtlProperties redisKeyTtlProperties;

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
        String hotProductKey = RedisKeyGenerator.hotProductKey();
        Map<String, Object> hotProductMap = RedisConnector.opsForHash().entries(hotProductKey);
        List<Product> hotProducts;
        if (hotProductMap.isEmpty()) {
            hotProducts = productMapper.selectOrderByDescSalesCountLimit(limit);
            List<Long> hotProductIdList = new ArrayList<>(hotProducts.size());
            Map<String, Object> redisMap = new HashMap<>(hotProducts.size());
            for (Product hotProduct : hotProducts) {
                Long hotProductId = hotProduct.getId();
                String hashKey = RedisKeyGenerator.hotProductHashKey(hotProductId);
                hotProductIdList.add(hotProductId);
                redisMap.put(hashKey, hotProduct);
            }
            String hotProductIdListJson = JacksonUtils.toJson(hotProductIdList);
            StringRedisConnector.opsForValue().set(RedisKeyGenerator.hotProductIdList(), hotProductIdListJson);
            RedisConnector.opsForHash().putAll(hotProductKey, redisMap);
        } else {
            hotProducts = hotProductMap.values().stream()
                    .map(hotProduct -> (Product) hotProduct)
                    .sorted(Comparator.comparing(Product::getSalesCount).reversed())
                    .collect(Collectors.toList());
        }
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
    @SuppressWarnings("unchecked")
    public Result getProductDetail(String productId, String userId) {
        if (StringUtils.isBlank(productId)) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        if (!bloomFilterUtils.contains(Long.valueOf(productId))) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        if (StringUtils.isBlank(userId)) {
            userId = DataConstant.NEGATIVE_ONE_STRING;
        }
        String productDetailKey = RedisKeyGenerator.productDetail(Long.valueOf(productId));
        String productCollectionKey = RedisKeyGenerator.productCollection(Long.valueOf(productId));
        Map<String, Object> productDetailMap = RedisConnector.opsForHash().entries(productDetailKey);
        Set<Object> userIdSet =(Set<Object>)(RedisConnector.opsForValue().get(productCollectionKey));
        if (productDetailMap.isEmpty()) {
            Product product = productMapper.selectByProductId(productId, userId);
            //空对象
            if (Objects.isNull(product)) {
                StringRedisConnector.opsForHash().putAll(productDetailKey, Map.of(Product.Fields.id, productId));
                return Result.error(MessageConstant.DATA_ERROR);
            }
            if (!StringUtils.equals(product.getIsCollection().toString(), CommonStatus.INACTIVE.getNumber().toString())) {
                product.setIsCollection(
                        CommonStatus.ACTIVE.getNumber());
            }
            Map<String, Object> productDetailResultMap = JacksonUtils.toMap(product);
            productDetailResultMap.put(Product.Fields.isCollection, CommonStatus.INACTIVE.getNumber());
            RedisConnector.opsForHash().putAll(productDetailKey, productDetailResultMap);
            StringRedisConnector.expire(productDetailKey, redisKeyTtlProperties.getProductDetailTtl(), TimeUnit.SECONDS);
            return Result.success(product);

        }
        //对空对象二次访问拦截
        if (productDetailMap.size() == 1) {
            return Result.error(MessageConstant.DATA_ERROR);

        }
        if (CollectionUtils.isEmpty(userIdSet)) {
            List<ProductCollection> productCollectionList = collectionService.lambdaQuery().eq(ProductCollection::getProductId, productId).list();
            userIdSet = productCollectionList.stream().map(ProductCollection::getUserId).collect(Collectors.toSet());
            RedisConnector.opsForValue().set(productCollectionKey, userIdSet);
            RedisConnector.expire(productDetailKey, redisKeyTtlProperties.getProductCollectionTtl(), TimeUnit.SECONDS);

        }
        Product resultProduct = JacksonUtils.fromMap(productDetailMap, Product.class);
        if (userIdSet.contains(Long.valueOf(userId))) {
            resultProduct.setIsCollection(CommonStatus.ACTIVE.getNumber());
        } else {
            resultProduct.setIsCollection(CommonStatus.INACTIVE.getNumber());

        }
        return Result.success(resultProduct);
    }


    /**
     * 根据 productIdSet 返回 productId与product映射Map集
     * 其中会更新redis缓存
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, Product> getProductDetailByProductIdSet(Set<Long> productIdSet) {
        productIdSet = new HashSet<>(productIdSet);
        if (productIdSet.isEmpty()) {
            return new HashMap<>(0);
        }
        List<String> keyList = productIdSet.stream().map(RedisKeyGenerator::productDetail).toList();
        List<Object> result = RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(@Nonnull RedisOperations<K, V> operations) throws DataAccessException {
                for (String key : keyList) {
                    operations.opsForHash().entries((K) key);
                }
                return null;
            }
        });
        if (result.isEmpty()) {
            List<Product> productList = getProductDetailAndSaveCacheByProductIdSet(productIdSet);
            return productListToMap(productList);
        }
        List<Product> redisProductList = result.stream().map(obj -> (Map<String, Object>) obj)
                .map(map -> JacksonUtils.fromMap(map, Product.class))
                .collect(Collectors.toList());
        Set<Long> redisProductIdSet = redisProductList.stream().map(Product::getId).collect(Collectors.toSet());
        productIdSet.removeAll(redisProductIdSet);
        if (productIdSet.isEmpty()) {
            return productListToMap(redisProductList);

        }
        List<Product> productList = getProductDetailAndSaveCacheByProductIdSet(productIdSet);
        redisProductList.addAll(productList);
        return productListToMap(redisProductList);

    }

    private Map<Long, Product> productListToMap(List<Product> productList) {
        if (Objects.isNull(productList) || productList.isEmpty()) {
            return new HashMap<>(0);

        }
        HashMap<Long, Product> resultMap = new HashMap<>(productList.size());
        for (Product product : productList) {
            resultMap.put(product.getId(), product);
        }
        return resultMap;
    }


    private List<Product> getProductDetailAndSaveCacheByProductIdSet(Set<Long> productIdSet) {
        List<Product> productList = productMapper.getProductDetailByProductIdSet(productIdSet);
        for (Product product : productList) {
            String key = RedisKeyGenerator.productDetail(product.getId());
            RedisConnector.setHashObject(key, product);
        }
        return productList;
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
        if (!bloomFilterUtils.contains(Long.valueOf(productId))) {
            return null;
        }
        String key = RedisKeyGenerator.productDetail(Long.valueOf(productId));
        List<ProductSpec> productSpecList = RedisConnector
                .getHashField(key, Product.Fields.specList, new TypeReference<>() {
                });
        if (Objects.isNull(productSpecList)) {
            String userId = DataConstant.NEGATIVE_ONE_STRING;
            Product product = productMapper.selectByProductId(productId, userId);
            RedisConnector.setHashObject(key, product);
            productSpecList = product.getSpecList();

        }
        if (productSpecList.isEmpty()) {
            return Result.error(MessageConstant.DATA_ERROR);

        }
        ProductSpec resultProductSpec = null;
        for (ProductSpec productSpec : productSpecList) {
            if (StringUtils.equals(productSpec.getId().toString(), specId)) {
                resultProductSpec = productSpec;
            }
        }
        if (Objects.isNull(resultProductSpec)) {
            return Result.error(MessageConstant.DATA_ERROR);

        }
        ProductSpecVO productSpecVO = copyMapper.productSpecToProductSpecVO(resultProductSpec);
        return Result.success(productSpecVO);
    }

    /**
     * 分类页面的商品列表滚动查询
     *
     * @param categoryId
     * @param beginProductId
     * @return
     */
    @Override
    public Result getCategoryProductList(String categoryId, String beginProductId) {
        String hashKey = RedisKeyGenerator.categoryTreeHashKey(Long.valueOf(categoryId));
        List<Long> secondCategoryIdList = RedisConnector
                .getHashField(RedisKeyGenerator.categoryTreeKey(), hashKey, new TypeReference<>() {});
        List<Product> productList;
        //一级分类 二级分类
        if (!Objects.isNull(secondCategoryIdList)) {
            productList = getProducts(secondCategoryIdList, beginProductId);

        } else {
            productList = getProducts(categoryId, beginProductId);
        }
        if (CollectionUtils.isEmpty(productList)) {
            return Result.success(CollectionUtils.emptyCollection());
        }
        List<SimpleProductVO> simpleProductVOs = productList.stream()
                .map(product -> copyMapper.productToSimpleProductVO(product)).collect(Collectors.toList());
        String endProductId = simpleProductVOs.get(simpleProductVOs.size() - 1).getId().toString();
        Collections.shuffle(simpleProductVOs);
        HashMap<String, Object> resultMap = new HashMap<>(2);
        resultMap.put(PRODUCT_LIST, simpleProductVOs);
        resultMap.put(END_PRODUCT_ID, endProductId);
        return Result.success(resultMap);
    }

    private List<Product> getProducts(String categoryId, String beginProductId) {
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
        return productList;
    }

    private List<Product> getProducts(List<Long> categoryIdList, String beginProductId) {
        if (categoryIdList.isEmpty()){
            return new ArrayList<>(0);

        }
        List<Product> productList;
        if (StringUtils.equals(beginProductId, Integer.toString(DataConstant.ZERO_INT))) {
            productList = lambdaQuery().in(Product::getCategoryId, categoryIdList)
                    .eq(Product::getStatus, CommonStatus.ACTIVE.getNumber()).orderByDesc(Product::getId)
                    .last("LIMIT " + DataConstant.PRODUCT_SCROLL_QUERY_NUMBER).list();
        } else {
            productList = lambdaQuery().in(Product::getCategoryId, categoryIdList)
                    .eq(Product::getStatus, CommonStatus.ACTIVE.getNumber())
                    .lt(Product::getId, beginProductId).orderByDesc(Product::getId)
                    .last("LIMIT " + DataConstant.PRODUCT_SCROLL_QUERY_NUMBER).list();
        }
        return productList;
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
            if (maxProductIdInDataOrDefault.equals(DataConstant.ZERO_LONG)) {
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
        if (scrollLoadedEndId.equals(minProductIdInDataOrDefault)) {
            sessionUtils.removeLoadedIdSet();
            sessionUtils.removeScrollLoadedEndId();
        }
        return Result.success(simpleProductVOS);
    }
}



