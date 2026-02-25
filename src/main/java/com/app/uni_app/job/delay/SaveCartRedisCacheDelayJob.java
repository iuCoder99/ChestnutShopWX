package com.app.uni_app.job.delay;

import com.app.uni_app.infrastructure.redis.properties.RedisKeyTtlProperties;
import com.app.uni_app.service.CartService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class SaveCartRedisCacheDelayJob {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisKeyTtlProperties redisKeyTtlProperties;

    @Resource
    private CartService cartService;

    @Resource
    @Qualifier("saveCartRedisCacheToMysqlThreadPool")
    private Executor threadPool;

    private RBlockingQueue<String> blockingQueue;
    private RDelayedQueue<String> delayedQueue;

    private static final String BLOCKING_QUEUE_NAME = "saveCartRedisCacheBlockingQueue";

    @PostConstruct
    private void init() {
        // 1. 初始化队列
        this.blockingQueue = redissonClient.getBlockingQueue(BLOCKING_QUEUE_NAME);
        this.delayedQueue = redissonClient.getDelayedQueue(blockingQueue);

        // 2. 启动后台消费线程
        startConsumer();
    }

    /**
     * 将用户ID加入延迟队列
     * 逻辑：如果任务已存在，先移除再添加，实现倒计时重置（防抖）
     */
    public void setUserIdToDelayedQueue(String userId) {
        // 1. 防抖逻辑：移除已存在的旧任务
        delayedQueue.remove(userId);

        // 2. 计算延迟时间（比缓存过期时间早 1 小时，确保同步时 Redis 还有数据）
        // 假设 redisKeyTtlProperties.getCartTtl() 单位是秒
        long delayInSeconds = redisKeyTtlProperties.getCartTtl() - 3600;
        
        // 安全检查，如果 TTL 设置过短，至少延迟 10 秒
        if (delayInSeconds < 0) {
            delayInSeconds = 10;
        }

        // 3. 添加新任务
        delayedQueue.offer(userId, delayInSeconds, TimeUnit.SECONDS);
        log.info("用户 {} 的购物车同步任务已加入延迟队列，将在 {} 秒后执行", userId, delayInSeconds);
    }

    /**
     * 后台消费逻辑
     */
    private void startConsumer() {
        threadPool.execute(() -> {
            log.info("购物车延迟同步消费者已启动...");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // take() 是阻塞的，会一直等到有任务到期
                    String userId = blockingQueue.take();
                    log.info("收到购物车同步任务，用户ID: {}", userId);
                    
                    // 执行同步逻辑
                    cartService.syncCartToMysql(userId);
                    
                } catch (InterruptedException e) {
                    log.warn("购物车同步消费者线程被中断，停止运行");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理购物车同步任务时发生异常: ", e);
                }
            }
        });
    }
}
