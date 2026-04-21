package com.app.uni_app.job.init;

import com.app.uni_app.job.sync.product.SyncProductFromEsToRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaxProductIdRedisCacheInitRunner implements ApplicationRunner {

    private final SyncProductFromEsToRedisService syncProductFromEsToRedisService;

    private static final String INIT_CACHE_BEGIN = "开始初始化商品加载范围区间...";
    private static final String INIT_CACHE_END = "初始化商品加载范围区间完成...";

    @Override
    public void run(ApplicationArguments args) {
        log.info(INIT_CACHE_BEGIN);
        syncProductFromEsToRedisService.syncMaxProductIdCache();
        log.info(INIT_CACHE_END);
    }
}
