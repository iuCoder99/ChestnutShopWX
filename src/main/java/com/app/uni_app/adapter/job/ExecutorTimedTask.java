package com.app.uni_app.adapter.job;

import com.app.uni_app.common.util.BloomFilterUtils;
import com.app.uni_app.common.util.CaffeineUtils;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.entity.Product;
import com.app.uni_app.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class ExecutorTimedTask {

    @Resource
    private CaffeineUtils caffeineUtils;

    @Resource
    private BloomFilterUtils bloomFilterUtils;

    @Resource
    private ProductService productService;

    private static final String PREFIX_SCHEDULED_EXECUTOR_TASK = "定时任务执行：";
    private static final String THREAD_NAME = " | 线程名: ";
    private static final String THREAD_ID = " | 线程ID: ";
    private static final String INIT_CACHE = "init...Cache...";

    /**
     * 初始化缓存
     */
    @PostConstruct
    public void onApplicationEvent() {
        log.info(INIT_CACHE);
        
        // 1. 初始化 Caffeine 本地缓存
        scheduledUpdateMaxProductIdInDataCache();
        
        // 2. 初始化布隆过滤器
        initBloomFilter();
    }
    
    private void initBloomFilter() {
        log.info("开始初始化布隆过滤器...");
        try {
            // 查询所有状态为启用的商品ID
            // 使用 listObjs 只查询 ID 字段，提高性能
            List<Object> productIds = productService.listObjs(
                new LambdaQueryWrapper<Product>()
                    .select(Product::getId)
                    .eq(Product::getStatus, CommonStatus.ACTIVE)
            );
            
            if (productIds != null && !productIds.isEmpty()) {
                for (Object id : productIds) {
                    if (id instanceof Long) {
                        bloomFilterUtils.add((Long) id);
                    }
                }
                log.info("布隆过滤器初始化完成，共加载 {} 个商品ID", productIds.size());
            } else {
                log.info("暂无商品数据，跳过布隆过滤器初始化");
            }
        } catch (Exception e) {
            log.error("布隆过滤器初始化失败", e);
        }
    }

    @Async("taskExecutor") // 关键：绑定自定义线程池
    @Scheduled(cron = "0 0 0/1 * * ?")  // 每小时0分0秒执行（如00:00、01:00、02:00...）
    public void scheduledUpdateMaxProductIdInDataCache() {
        System.out.println(PREFIX_SCHEDULED_EXECUTOR_TASK + LocalDateTime.now()
                + THREAD_NAME + Thread.currentThread().getName()
                + THREAD_ID + Thread.currentThread().getId());
        caffeineUtils.updateMaxAndMinProductIdInData();
    }
}
