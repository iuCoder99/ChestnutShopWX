package com.app.uni_app.common.util;

import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class BloomFilterUtils {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 商品ID布隆过滤器 Key
     */
    private static final String PRODUCT_BLOOM_FILTER = "product:bloom:filter";

    /**
     * 获取或初始化布隆过滤器
     * 预期元素数量: 10000 (根据实际业务量调整)
     * 误判率: 0.01 (1%)
     */
    public RBloomFilter<Long> getProductBloomFilter() {
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(PRODUCT_BLOOM_FILTER);
        // 如果不存在则初始化
        // expectedInsertions: 预期插入数量
        // falseProbability: 误判率
        bloomFilter.tryInit(10000L, 0.01);
        return bloomFilter;
    }

    /**
     * 判断 ID 是否可能存在
     * @param id 商品 ID
     * @return true 可能存在, false 一定不存在
     */
    public boolean contains(Long id) {
        return getProductBloomFilter().contains(id);
    }

    /**
     * 添加 ID 到布隆过滤器
     * @param id 商品 ID
     * @return true 添加成功, false 已存在
     */
    public boolean add(Long id) {
        return getProductBloomFilter().add(id);
    }
}
