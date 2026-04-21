package com.app.uni_app.service.impl;


import com.app.uni_app.aop.annotation.common.ParamCheckAnnotation;
import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.*;
import com.app.uni_app.common.util.BloomFilterUtils;
import com.app.uni_app.common.util.CaffeineUtils;
import com.app.uni_app.common.util.JacksonUtils;
import com.app.uni_app.common.util.SessionUtils;
import com.app.uni_app.infrastructure.es.common.mapstruct.EsCopyMapper;
import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.service.ProductDocumentService;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.connect.StringRedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.properties.RedisCacheCountProperties;
import com.app.uni_app.infrastructure.redis.properties.RedisCacheTtlProperties;
import com.app.uni_app.infrastructure.redis.service.ProductRedisCacheService;
import com.app.uni_app.infrastructure.rocketmq.constant.failed.MqFailedMessageConstant;
import com.app.uni_app.infrastructure.rocketmq.constant.product.MqProductConstant;
import com.app.uni_app.mapper.ProductMapper;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.emums.ProductSortTypeEnum;
import com.app.uni_app.pojo.entity.MqConsumerFailedMsg;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.pojo.entity.ProductCollection;
import com.app.uni_app.pojo.entity.ProductSpec;
import com.app.uni_app.pojo.vo.ProductSpecVO;
import com.app.uni_app.pojo.vo.SimpleProductVO;
import com.app.uni_app.service.CollectionService;
import com.app.uni_app.service.MqConsumerFailedMsgService;
import com.app.uni_app.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    private final ProductMapper productMapper;


    private final CollectionService collectionService;


    private final ProductDocumentService productDocumentService;


    private final ProductRedisCacheService productRedisCacheService;


    private final RedisCacheCountProperties redisCacheCountProperties;


    private final CopyMapper copyMapper;


    private final EsCopyMapper esCopyMapper;


    private final RocketMQTemplate rocketMQTemplate;


    private final SessionUtils sessionUtils;


    private final CaffeineUtils caffeineUtils;


    private final BloomFilterUtils bloomFilterUtils;


    private final MqConsumerFailedMsgService mqConsumerFailedMsgService;


    private final RedisCacheTtlProperties redisCacheTtlProperties;

    private static final String PRODUCT_LIST = "productList";
    private static final String END_PRODUCT_ID = "endProductId";


    /**
     * 获取热门商品
     * 按照销量进行排名
     * @param limit 展示数量
     * @return 返回商品实体类
     */
    @Override
    public List<ProductDocument> getHotProduct(Integer limit) {
        if (limit > redisCacheCountProperties.getHotProductCacheSize()){
            throw new RuntimeException("超出热门商品最大缓存数量");
        }
        List<ProductDocument> hotProductList = productRedisCacheService.getHotProduct();
        List<Long> hotProductIdList = productRedisCacheService.getHotProductIdList();
        List<ProductDocument> productDocumentResultList = new ArrayList<>(hotProductIdList.size());
        HashMap<Long, ProductDocument> mapping = new HashMap<>(hotProductList.size());
        for (ProductDocument productDocument : hotProductList) {
            mapping.put(productDocument.getId(),productDocument);
        }
        for (Long id : hotProductIdList) {
            ProductDocument productDocument = mapping.get(id);
            productDocumentResultList.add(productDocument);
        }
       return productDocumentResultList.stream().limit(limit).toList();
    }

    /**
     * 获取列表商品简单介绍
     * 进行 es 查询 获取数据 , 如果没有查询为 null , 在数据库进行查询 ,再进行 mq 消息通知进行数据同步
     * @param productIds 列表商品 ids: 1,2,3
     * @return 简单商品列表
     */
    @Override
    public Result<List<SimpleProductVO>> getBriefProduct(String productIds) {
        if (StringUtils.isBlank(productIds)) {
            return Result.success(new ArrayList<>(0));
        }
        List<Long> productIdList = Arrays.stream(StringUtils.split(productIds, ",")).map(Long::valueOf).toList();
        Map<Long, SimpleProductVO> resultMap = new HashMap<>(productIdList.size());
        List<SimpleProductVO> resultList = new ArrayList<>(productIdList.size());
        for (Long id : productIdList) {
            resultMap.put(id, null);
        }
        List<SimpleProductVO> simpleProductVOListByEs = productDocumentService.getProductDocumentByIdList(productIdList).stream().map(esCopyMapper::ProductDocumentToSimpleProductVO).toList();
        for (SimpleProductVO s : simpleProductVOListByEs) {
            resultMap.put(s.getId(), s);
        }
        if (productIdList.size() == simpleProductVOListByEs.size()) {
            for (Long id : productIdList) {
                SimpleProductVO simpleProductVO = resultMap.get(id);
                resultList.add(simpleProductVO);
            }
            return Result.success(resultList);
        }
        List<Long> needQueryBySQLIdList = new ArrayList<>();
        resultMap.forEach((key, value) -> {
            if (Objects.isNull(value)) {
                needQueryBySQLIdList.add(key);
            }
        });
        List<Product> list = productMapper.getBriefProduct(needQueryBySQLIdList);
        asyncSaveProductDocumentBySendMqMessage(list, 0);
        list.stream().map(copyMapper::productToSimpleProductVO)
                .forEach(simpleProductVO -> resultMap.put(simpleProductVO.getId(), simpleProductVO));
        for (int i = 0; i < productIdList.size(); i++) {
            Long id = productIdList.get(i);
            SimpleProductVO simpleProductVO = resultMap.get(id);
            resultList.add(i, simpleProductVO);
        }
        return Result.success(resultList);

    }

    /**
     * 异步通知 mq 同步商品文档到 es
     * 异常发送三次
     * @param productList 商品文档列表
     */
    void asyncSaveProductDocumentBySendMqMessage(List<Product> productList, int retryCount) {
        List<ProductDocument> productDocumentList = productList.stream()
                .map(esCopyMapper::ProductToProductDocument)
                .toList();
        String destination = MqProductConstant.TOPIC_PRODUCT + ":" + MqProductConstant.TAG_PRODUCT_DOCUMENT_SYNC;
        int maxRetry = 2;
        rocketMQTemplate.asyncSend(destination, productDocumentList, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
            }

            @Override
            public void onException(Throwable throwable) {
                if (retryCount < maxRetry) {
                    asyncSaveProductDocumentBySendMqMessage(productList, retryCount + 1);
                } else {
                    MqConsumerFailedMsg failedMsg = MqConsumerFailedMsg.builder()
                            .topic(MqProductConstant.TOPIC_PRODUCT)
                            .tag(MqProductConstant.TAG_PRODUCT_DOCUMENT_SYNC)
                            .errorMsg(MqFailedMessageConstant.MQ_FAILED_ASYNC_SEND)
                            .body("同步商品数据到es失败,失败商品: " + productList)
                            .retryCount(retryCount)
                            .build();
                    mqConsumerFailedMsgService.save(failedMsg);
                }
            }
        });
    }


    /**
     * 获取商品详情
     * @param productId 商品 id
     * @param userId 用户 id ,如果用户未登录 ,这里是 null
     * @return 商品实体类
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result<?> getProductDetail(String productId, String userId) {
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
        Set<Object> userIdSet = (Set<Object>) (RedisConnector.opsForValue().get(productCollectionKey));
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
            StringRedisConnector.expire(productDetailKey, redisCacheTtlProperties.getProductDetailTtl(), TimeUnit.SECONDS);
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
            RedisConnector.expire(productDetailKey, redisCacheTtlProperties.getProductCollectionTtl(), TimeUnit.SECONDS);

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
     * 游标查询指定分类下的简单商品列表, 通过 es 进行查询
     * @param cursorCommonEntity 游标参数实体
     * @return 游标返回实体
     */
    @Override
    public CursorCommonResult getCategorySimpleProduct(CursorCommonEntity cursorCommonEntity, Long categoryId, boolean isFirstCategoryId) {
        String sortType = cursorCommonEntity.getSortType();
        Long sortId = cursorCommonEntity.getSortId();
        String sortValue = cursorCommonEntity.getSortValue();
        ProductSortTypeEnum productSortTypeEnum = ProductSortTypeEnum.getByValue(sortType);
        sortValue = ProductSortTypeEnum.filterFormatSortValue(productSortTypeEnum, sortValue);
        Integer querySize = cursorCommonEntity.getQuerySize();
        List<ProductDocument> productDocuments = productDocumentService.searchByCursorByCategoryId(
                querySize, productSortTypeEnum, sortValue, sortId, categoryId, isFirstCategoryId);
        return getCursorCommonResult(productDocuments, querySize, productSortTypeEnum, sortType);

    }



    /**
     * 游标分类查询简介商品列表
     * @param cursorCommonEntity 分类游标通用实体
     * @param keyword 关键词
     * @return 分类游标通用实体
     */
    @Override
    @ParamCheckAnnotation
    public CursorCommonResult searchProductList(CursorCommonEntity cursorCommonEntity, String keyword) {
        Integer querySize = cursorCommonEntity.getQuerySize();
        String sortType = cursorCommonEntity.getSortType();
        Long sortId = cursorCommonEntity.getSortId();
        String sortValue = cursorCommonEntity.getSortValue();
        ProductSortTypeEnum productSortTypeEnum = ProductSortTypeEnum.getByValue(sortType);
        sortValue = ProductSortTypeEnum.filterFormatSortValue(productSortTypeEnum, sortValue);
        List<ProductDocument> productDocuments = productDocumentService.searchByCursorByName(querySize, productSortTypeEnum, sortValue, sortId, keyword);
        return getCursorCommonResult(productDocuments, querySize, productSortTypeEnum, sortType);

    }

    /**
     * 游标结果封装方法
     * @param productDocuments 在商品文档服务 查询出来的 原始商品文档
     * @param querySize 查询数量
     * @param productSortTypeEnum 商品排序种类枚举
     * @param sortType 排序种类字符串
     * @return 游标结果
     */
    private CursorCommonResult getCursorCommonResult(List<ProductDocument> productDocuments, Integer querySize, ProductSortTypeEnum productSortTypeEnum, String sortType) {
        boolean isEnd = false;
        if (productDocuments.isEmpty()) {
            return CursorCommonResult.builder()
                    .isEnd(true)
                    .list(Collections.emptyList())
                    .build();
        }
        if (querySize > productDocuments.size()) {
            isEnd = true;
        }
        ProductDocument productDocument = productDocuments.get(productDocuments.size() - 1);
        String sortValueByProductDocument = ProductSortTypeEnum.getSortValueByProductDocument(productSortTypeEnum, productDocument);
        CursorCommonEntity cursorCommonEntityResult = CursorCommonEntity.builder()
                .sortType(sortType)
                .querySize(querySize)
                .sortId(productDocument.getId())
                .sortValue(sortValueByProductDocument)
                .build();
        List<SimpleProductVO> simpleProductVOS = productDocuments.stream().map(esCopyMapper::ProductDocumentToSimpleProductVO).toList();
        return CursorCommonResult.builder()
                .isEnd(isEnd)
                .list(simpleProductVOS)
                .cursorCommonEntity(cursorCommonEntityResult)
                .build();
    }

    /**
     * 基于商品名字, 进行es查询
     * @param productName 商品名字
     * @param limit 查询数
     * @return 返回商品文档列表
     */
    @Override
    public List<ProductDocument> getProductRelated(String productName, Integer limit) {
        List<ProductDocument> allMatchProductDocument = productDocumentService.getProductDocumentByProductNameKeyword(productName, limit + 1);
        for (ProductDocument productDocument : allMatchProductDocument) {
            if (productDocument.getName().equals(productName)) {
                allMatchProductDocument.remove(productDocument);
                break;
            }
        }
        return allMatchProductDocument;


    }

    /**
     * 获取商品规格价格
     *
     * @param productId
     * @param specId
     * @return
     */
    @Override
    public Result<?> getProductSpecPrice(String productId, String specId) {
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
    public Result getCategoryProductList(String categoryId, String beginProductId, String sortType) {
        String hashKey = RedisKeyGenerator.categoryTreeHashKey(Long.valueOf(categoryId));
        List<Long> secondCategoryIdList = RedisConnector
                .getHashField(RedisKeyGenerator.categoryTreeKey(), hashKey, new TypeReference<>() {
                });
        List<Product> productList;
        //一级分类
        if (!Objects.isNull(secondCategoryIdList)) {
            productList = getProducts(secondCategoryIdList, beginProductId);

        } else {
            //二级分类
            productList = getProducts(categoryId, beginProductId);
        }
        if (CollectionUtils.isEmpty(productList)) {
            return Result.success(CollectionUtils.emptyCollection());
        }
        ProductSortTypeEnum productSortTypeEnum = ProductSortTypeEnum.getByValue(sortType);
        List<SimpleProductVO> simpleProductVOs = productList.stream()
                .sorted((p1, p2) -> ProductSortTypeEnum.compare(p1, p2, productSortTypeEnum))
                .map(copyMapper::productToSimpleProductVO)
                .collect(Collectors.toList());
        String endProductId = simpleProductVOs.get(simpleProductVOs.size() - 1).getId().toString();
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
        if (categoryIdList.isEmpty()) {
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
     * 判断是否是首次查询,是获取随机开始id ,然后进行es 游标查询
     * @return 游标返回实体
     */
    @Override
    public SimpleCursorCommonResult getSimpleProductByScrollQuery(Long beginId , Integer querySize) {
        if (Objects.isNull(beginId)) {
            Long maxProductId = productRedisCacheService.getMaxProductId();
            long maxBeginId = Math.round(maxProductId * DataConstant.QUERY_SECURITY_NUMBER);
            maxBeginId = Math.max(maxBeginId, 2);
            beginId = ThreadLocalRandom.current().nextLong(1, maxBeginId);
        }
            List<SimpleProductVO> resultList = productDocumentService.searchLimitAfterProductId(querySize, beginId).stream()
                    .map(esCopyMapper::ProductDocumentToSimpleProductVO)
                    .collect(Collectors.toList());
            if (resultList.isEmpty()) {
                return SimpleCursorCommonResult.builder().list(Collections.emptyList())
                        .isEnd(true)
                        .build();
            }
            Long endId = resultList.get(resultList.size() - 1).getId();
            boolean isEnd = resultList.size() < querySize;
           Collections.shuffle(resultList);
            SimpleCursorCommonEntity simpleCursorCommonEntity = SimpleCursorCommonEntity.builder()
                    .querySize(querySize)
                    .sortId(endId)
                    .build();
            return SimpleCursorCommonResult.builder()
                    .list(resultList)
                    .isEnd(isEnd)
                    .simpleCursorCommonEntity(simpleCursorCommonEntity)
                    .build();

        }
    }





