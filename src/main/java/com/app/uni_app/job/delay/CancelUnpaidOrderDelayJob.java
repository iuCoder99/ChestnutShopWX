package com.app.uni_app.job.delay;

import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.redis.properties.RedisKeyTtlProperties;
import com.app.uni_app.service.impl.OrderServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CancelUnpaidOrderDelayJob {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisKeyTtlProperties redisKeyTtlProperties;

    @Resource
    @Qualifier("cancelUnpaidOrderExecutor")
    private Executor threadPool;

    @Resource
    private ApplicationContext applicationContext;


    private RBlockingQueue<String> blockingQueue;
    private RDelayedQueue<String> delayedQueue;

    private static final String BLOCKING_QUEUE_NAME = "cancelUnpaidOrderBlockingQueue";
    private static final String ORDER_CANCEL_REASON = "订单超时未支付，自动取消";


    @PostConstruct
    private void init() {
        RBlockingQueue<String> blockingQueue = redissonClient.getBlockingQueue(BLOCKING_QUEUE_NAME);
        RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        this.blockingQueue = blockingQueue;
        this.delayedQueue = delayedQueue;

    }

    public void setUnpaidOrderNoToDelayQueue(String orderNO) {
        log.info("order:{},添加延迟任务成功",orderNO);
        delayedQueue.remove(orderNO);

        long orderTtl = redisKeyTtlProperties.getOrderTtl();
        delayedQueue.offer(orderNO, orderTtl, TimeUnit.SECONDS);

        startConsumer();
    }

    public void setPaidOrderNoToCancelDelayQueue(String orderNo){
        delayedQueue.remove(orderNo);
    }


    private void startConsumer() {
        threadPool.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String orderNo = blockingQueue.take();
                    boolean isSuccess = applicationContext.getBean("orderServiceImpl",OrderServiceImpl.class).cancelOrderCommon(orderNo, ORDER_CANCEL_REASON);
                    RedisConnector.delete(RedisKeyGenerator.orderKey(orderNo));
                    if (!isSuccess) {
                        startConsumer();
                    }
                } catch (InterruptedException e) {
                    log.warn("处理订单支付超时消费者线程被中断，停止运行");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理订单支付超时任务时发生异常: ", e);
                }


            }
        });

    }
}