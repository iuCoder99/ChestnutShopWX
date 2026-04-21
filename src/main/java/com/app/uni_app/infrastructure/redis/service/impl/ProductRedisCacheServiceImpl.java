package com.app.uni_app.infrastructure.redis.service.impl;

import com.app.uni_app.infrastructure.es.document.ProductDocument;
import com.app.uni_app.infrastructure.es.service.ProductDocumentService;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.constant.bucket.BucketConstant;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyBucketGenerator;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyCopyGenerator;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.properties.RedisBucketTtlProperties;
import com.app.uni_app.infrastructure.redis.service.ProductRedisCacheService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRedisCacheServiceImpl implements ProductRedisCacheService {

    private final RedissonClient redissonClient;

    private final RedisBucketTtlProperties redisBucketTtlProperties;

    private final ProductDocumentService productDocumentService;

    @Resource(name = "executorSchedulerCommon")
    private ThreadPoolTaskExecutor threadPoolExecutor;


    /**
     * 查询热门商品列表（双缓存 + 读写标记控制）
     */
    @Override
    public List<ProductDocument> getHotProduct() {
        String key = RedisKeyGenerator.hotProductKey();
        RBucket<BucketConstant.BucketSign> bucket = redissonClient.getBucket(RedisKeyBucketGenerator.generate(key));

        if (!bucket.isExists() || BucketConstant.BucketThreadType.READ_THREAD.equals(bucket.get().bucketThreadType())) {
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucket.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                return RedisConnector.opsForHash()
                        .entries(key)
                        .values()
                        .stream()
                        .map(o -> (ProductDocument) o)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("查询redis热门商品主数据失败", e);
            } finally {
                if (bucket.isExists() && bucketSign.equals(bucket.get())) {
                    bucket.delete();
                }
            }
        } else {
            String copyKey = RedisKeyCopyGenerator.copyKey(key);
            RBucket<BucketConstant.BucketSign> bucketCopy = redissonClient.getBucket(RedisKeyBucketGenerator.generate(copyKey));
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucketCopy.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                return RedisConnector.opsForHash()
                        .entries(copyKey)
                        .values()
                        .stream()
                        .map(o -> (ProductDocument) o)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("查询redis热门商品副本数据失败", e);
            } finally {
                if (bucketCopy.isExists() && bucketSign.equals(bucketCopy.get())) {
                    bucketCopy.delete();
                }
            }
        }
        return null;
    }

    /**
     * 查询热门商品ID列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Long> getHotProductIdList() {
        String key = RedisKeyGenerator.hotProductIdList();
        RBucket<BucketConstant.BucketSign> bucket = redissonClient.getBucket(RedisKeyBucketGenerator.generate(key));

        if (!bucket.isExists() || BucketConstant.BucketThreadType.READ_THREAD.equals(bucket.get().bucketThreadType())) {
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucket.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                 return  (List<Long>) RedisConnector.opsForValue().get(key);
            } catch (Exception e) {
                log.error("查询redis热门商品ID列表失败", e);
            } finally {
                if (bucket.isExists() && bucketSign.equals(bucket.get())) {
                    bucket.delete();
                }
            }
        } else {
            String copyKey = RedisKeyCopyGenerator.copyKey(key);
            RBucket<BucketConstant.BucketSign> bucketCopy = redissonClient.getBucket(RedisKeyBucketGenerator.generate(copyKey));
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucketCopy.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                return  (List<Long>) RedisConnector.opsForValue().get(copyKey);
            } catch (Exception e) {
                log.error("查询redis热门商品ID副本列表失败", e);
            } finally {
                if (bucketCopy.isExists() && bucketSign.equals(bucketCopy.get())) {
                    bucketCopy.delete();
                }
            }
        }
        return null;
    }

    /**
     * 查询单个热门商品
     */
    @Override
    public ProductDocument getHotProduct(Long productId) {
        String key = RedisKeyGenerator.hotProductKey();
        RBucket<BucketConstant.BucketSign> bucket = redissonClient.getBucket(RedisKeyBucketGenerator.generate(key));

        if (!bucket.isExists() || BucketConstant.BucketThreadType.READ_THREAD.equals(bucket.get().bucketThreadType())) {
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucket.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                String hashKey = RedisKeyGenerator.hotProductHashKey(productId);
                return (ProductDocument) RedisConnector.opsForHash().get(key, hashKey);
            } catch (Exception e) {
                log.error("查询redis单个热门商品失败", e);
            } finally {
                if (bucket.isExists() && bucketSign.equals(bucket.get())) {
                    bucket.delete();
                }
            }
        } else {
            String copyKey = RedisKeyCopyGenerator.copyKey(key);
            RBucket<BucketConstant.BucketSign> bucketCopy = redissonClient.getBucket(RedisKeyBucketGenerator.generate(copyKey));
            BucketConstant.BucketSign bucketSign = new BucketConstant.BucketSign(
                    BucketConstant.BucketThreadType.READ_THREAD,
                    UUID.randomUUID().toString()
            );
            try {
                bucketCopy.set(bucketSign, Duration.ofSeconds(redisBucketTtlProperties.getHotProductReadBucketTtl()));
                String hashCopyKey = RedisKeyCopyGenerator.copyKey(RedisKeyGenerator.hotProductHashKey(productId));
                return (ProductDocument) RedisConnector.opsForHash().get(copyKey, hashCopyKey);
            } catch (Exception e) {
                log.error("查询redis单个热门商品副本失败", e);
            } finally {
                if (bucketCopy.isExists() && bucketSign.equals(bucketCopy.get())) {
                    bucketCopy.delete();
                }
            }
        }
        return null;
    }


    @Override
    public Long getMaxProductId() {
        String maxProductIdKey = RedisKeyGenerator.maxProductId();
        Object maxProductIdObject = RedisConnector.opsForValue().get(maxProductIdKey);
        if (Objects.isNull(maxProductIdObject)) {
            threadPoolExecutor.execute(this::initMaxProductId);
            return productDocumentService.getMaxProductDocumentId();
        }
        return Long.valueOf(maxProductIdObject.toString());
    }

    @Override
    public void initMaxProductId() {
        Long maxProductDocumentId = productDocumentService.getMaxProductDocumentId();
        String key = RedisKeyGenerator.maxProductId();
        RedisConnector.opsForValue().set(key,maxProductDocumentId);
    }
}