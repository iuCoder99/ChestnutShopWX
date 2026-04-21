package com.app.uni_app.job.init;

import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.service.ProductDocumentService;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.constant.bucket.BucketConstant;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyBucketGenerator;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.properties.RedisCacheCountProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 初始化热门商品缓存
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HotProductInitRunner implements ApplicationRunner {

    private final ProductDocumentService productDocumentService;

    private final RedisCacheCountProperties redisCacheCountProperties;

    private final RedissonClient redissonClient;


    //从 es 初始化热门商品缓存到 redis
    @Override
    @SuppressWarnings("unchecked")
    public void run(ApplicationArguments args) throws Exception {
        log.info("初始化热门商品缓存... 初始化数量{}", redisCacheCountProperties.getHotProductCacheSize());
        List<ProductDocument> productDocuments = productDocumentService.searchLimitHotProductDocument(redisCacheCountProperties.getHotProductCacheSize());
        ArrayList<Long> hotProductIdList = new ArrayList<>(productDocuments.size());
        productDocuments.forEach(doc -> hotProductIdList.add(doc.getId()));
        RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                if (Objects.isNull(operations)){
                    log.error("热门商品缓存初始化失败...");
                    return null;
                }
                String key = RedisKeyGenerator.hotProductKey();
                for (ProductDocument productDocument : productDocuments) {
                    String hashKey = RedisKeyGenerator.hotProductHashKey(productDocument.getId());
                    operations.opsForHash().put((K) key, hashKey, productDocument);
                }
                String hotProductIdListKey = RedisKeyGenerator.hotProductIdList();
                operations.opsForValue().set((K) hotProductIdListKey, (V) hotProductIdList);
                return null;
            }
        });
        log.info("初始化热门商品 redis缓存完成...");
        log.info("初始化热门商品idList redis缓存完成...");
        log.info("开始初始化热门商品 bucket...");
        RBucket<BucketConstant.BucketSign> hotProductBucket = redissonClient.getBucket(RedisKeyBucketGenerator.generate(RedisKeyGenerator.hotProductKey()));
        RBucket<BucketConstant.BucketSign> hotIdListProductBucket = redissonClient.getBucket(RedisKeyBucketGenerator.generate(RedisKeyGenerator.hotProductIdList()));
        hotProductBucket.set(new BucketConstant.BucketSign(BucketConstant.BucketThreadType.READ_THREAD,null));
        hotIdListProductBucket.set(new BucketConstant.BucketSign(BucketConstant.BucketThreadType.READ_THREAD,null));
        log.info("初始化热门商品 bucket完成...");

    }
}
