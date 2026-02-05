package com.app.uni_app.infrastructure.thread.ThreadPoolConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class PayExceptionThreadPool {
    /**
     * 核心线程数：线程池常驻的最小线程数，即使空闲也不会被回收（除非设置了allowCoreThreadTimeOut）
     * 推荐值：CPU密集型任务（核心线程数 = CPU核心数 + 1）；IO密集型任务（核心线程数 = CPU核心数 * 2）
     */
    private static final int CORE_POOL_SIZE = 4;

    /**
     * 最大线程数：线程池能容纳的最大线程数，当任务队列满了之后，会创建新线程执行任务
     */
    private static final int MAX_POOL_SIZE = 16;

    /**
     * 队列容量：用于存放等待执行任务的队列容量
     */
    private static final int QUEUE_CAPACITY = 100;

    /**
     * 空闲线程存活时间：非核心线程空闲超过该时间会被回收（单位：秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;

    /**
     * 线程池Bean名称（可通过@Qualifier("businessThreadPool")指定注入）
     * @return 线程池执行器
     */
    @Bean(name = "solvePayExceptionThreadPool")
    public Executor solvePayExceptionThreadPool() {
        // Spring提供的ThreadPoolTaskExecutor，封装了原生ThreadPoolExecutor，更易用
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        // 设置最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        // 设置队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        // 设置空闲线程存活时间
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        // 设置线程名称前缀（便于日志排查问题，非常重要！）
        executor.setThreadNamePrefix("Business-Thread-");
        // 设置任务拒绝策略：当线程池和队列都满了，如何处理新任务
        // ThreadPoolExecutor.CallerRunsPolicy：由提交任务的线程自己执行（避免任务丢失，企业开发常用）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化线程池
        executor.initialize();
        return executor;
    }
}
