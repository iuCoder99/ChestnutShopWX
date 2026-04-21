package com.app.uni_app.job.sync.product;

public interface SyncProductFromEsToRedisService {
    /**
     * 同步热门商品缓存
     */
    void syncHotProductCache();

    /**
     * 同步商品最大id缓存
     */
    void syncMaxProductIdCache();
}
