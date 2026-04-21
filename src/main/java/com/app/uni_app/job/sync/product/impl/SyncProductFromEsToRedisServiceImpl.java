package com.app.uni_app.job.sync.product.impl;

import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.service.ProductDocumentService;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.constant.bucket.BucketConstant;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyBucketGenerator;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyCopyGenerator;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.properties.RedisBucketTtlProperties;
import com.app.uni_app.infrastructure.redis.properties.RedisCacheCountProperties;
import com.app.uni_app.infrastructure.redis.service.ProductRedisCacheService;
import com.app.uni_app.job.sync.product.SyncProductFromEsToRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SyncProductFromEsToRedisServiceImpl implements SyncProductFromEsToRedisService {

    private final ProductDocumentService productDocumentService;

    private final RedisCacheCountProperties redisCacheCountProperties;

    private final RedissonClient redissonClient;

    private final RedisBucketTtlProperties redisBucketTtlProperties;

    private final ProductRedisCacheService productRedisCacheService;

    private static final String errorMessage = "es 查询热门数据异常 ... es -> redis 数据同步失败 ... , ";
    private static final String successMessage = "es -> redis 热门商品数据同步成功...";

    // 自旋最大等待时间（5秒）
    private static final long MAX_WAIT_MILLIS = 5000;

    /**
     * es->redis 同步热门商品数据
     * 关于热门商品缓存更新策略
     * 更新线程先创建缓存副本 ,然后对缓存加锁, 读线程查到更新锁,去查副本, 然后更新线程更新完缓存, 释放锁,不删除副本
     */
    public void syncHotProductCache() {
        List<ProductDocument> productDocuments = productDocumentService.searchLimitHotProductDocument(redisCacheCountProperties.getHotProductCacheSize());
        if (productDocuments.isEmpty()) {
            log.error(errorMessage + "es 查询热门商品文档为空 ");
            return;
        }
        // 先更新副本
        updateHotProductCache(productDocuments, true);
        // 再更新主缓存
        updateHotProductCache(productDocuments, false);
        log.info(successMessage);
    }

    /**
     * 更新热门商品缓存
     * @param productDocuments 热门商品文档
     * @param isUpdateCopyCache 本次方法调用是更新缓存副本,还是更新缓存
     */
    @SuppressWarnings({"unchecked", "BusyWait"})
    private void updateHotProductCache(List<ProductDocument> productDocuments, boolean isUpdateCopyCache) {
        String key = isUpdateCopyCache
                ? RedisKeyCopyGenerator.copyKey(RedisKeyGenerator.hotProductKey())
                : RedisKeyGenerator.hotProductKey();

        String hotProductIdListKey = isUpdateCopyCache
                ? RedisKeyCopyGenerator.copyKey(RedisKeyGenerator.hotProductIdList())
                : RedisKeyGenerator.hotProductIdList();

        List<Long> hotProductIds = new ArrayList<>(productDocuments.size());
        productDocuments.forEach(doc -> hotProductIds.add(doc.getId()));

        if (isUpdateCopyCache) {
            RBucket<BucketConstant.BucketSign> flagBucket = redissonClient.getBucket(RedisKeyBucketGenerator.generate(key));
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.WRITE_THREAD,
                    UUID.randomUUID().toString()
            );

            // 等待标记位消失 + 最大等待超时保护
            long startWait = System.currentTimeMillis();
            while (flagBucket.isExists()) {
                try {
                    Thread.sleep(50);
                    // 超时保护，防止死循环
                    if (System.currentTimeMillis() - startWait > MAX_WAIT_MILLIS) {
                        log.error("等待副本缓存标记位超时，放弃更新");
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("等待副本缓存标记位被中断", e);
                    return;
                }
            }
            flagBucket.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductWriteBucketTtl()));
            try {
                RedisConnector.executePipelined(new SessionCallback<>() {
                    @Override
                    public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                        if (operations == null) {
                            log.error(errorMessage + "创建热门商品缓存副本失败，Redis连接为空");
                            return null;
                        }
                        operations.delete((K) key);
                        operations.delete((K) hotProductIdListKey);
                        operations.opsForValue().set((K) hotProductIdListKey, (V) hotProductIds);
                        for (ProductDocument doc : productDocuments) {
                            String copyHashKey = RedisKeyCopyGenerator.copyKey(RedisKeyGenerator.hotProductHashKey(doc.getId()));
                            operations.opsForHash().put((K) key, copyHashKey, doc);
                        }
                        return null;
                    }
                });
                log.info("热门商品缓存副本更新成功");
            } catch (Exception e) {
                log.error(errorMessage + "更新热门商品缓存副本异常", e);
            } finally {
                // 归属校验删除
                if (flagBucket.isExists() && bucketSign.equals(flagBucket.get())) {
                    flagBucket.delete();
                }
            }
            return;
        }

        RBucket<BucketConstant.BucketSign> flag = redissonClient.getBucket(RedisKeyBucketGenerator.generate(key));
        BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                BucketConstant.BucketThreadType.WRITE_THREAD,
                UUID.randomUUID().toString()
        );

        // 等待 + 超时保护
        long startWait = System.currentTimeMillis();
        while (flag.isExists()) {
            try {
                Thread.sleep(50);
                if (System.currentTimeMillis() - startWait > MAX_WAIT_MILLIS) {
                    log.error("等待主缓存标记位超时，放弃更新");
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待主缓存标记位被中断", e);
                return;
            }
        }

        // 设置标记 + 自动过期
        flag.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductWriteBucketTtl()));

        try {
            RedisConnector.executePipelined(new SessionCallback<>() {
                @Override
                public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                    if (Objects.isNull(operations)) {
                        log.error(errorMessage + "更新热门商品缓存失败..., Redis 连接为空");
                        return null;
                    }
                    operations.delete((K) key);
                    operations.delete((K) hotProductIdListKey);
                    operations.opsForValue().set((K) hotProductIdListKey, (V) hotProductIds);
                    for (ProductDocument doc : productDocuments) {
                        String hashKey = RedisKeyGenerator.hotProductHashKey(doc.getId());
                        operations.opsForHash().put((K) key, hashKey, doc);
                    }
                    return null;
                }
            });
            log.info("热门商品主缓存更新成功");
        } catch (Exception e) {
            log.error(errorMessage + "更新热门商品主缓存异常", e);
        } finally {
            if (flag.isExists() && bucketSign.equals(flag.get())) {
                flag.delete();
            }
        }
    }


    /**
     * 从es同步最大商品id到redis缓存
     */
    @Override
    public void syncMaxProductIdCache() {
     productRedisCacheService.initMaxProductId();
    }
}