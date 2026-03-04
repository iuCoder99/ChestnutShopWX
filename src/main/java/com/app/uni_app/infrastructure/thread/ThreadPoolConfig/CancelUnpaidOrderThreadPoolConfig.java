package com.app.uni_app.infrastructure.thread.ThreadPoolConfig;

import com.app.uni_app.infrastructure.thread.ThreadPoolConstant.ThreadPoolConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class CancelUnpaidOrderThreadPoolConfig {

    private static final String THREAD_POOL_NAME ="cancelUnpaidOrder";

    @Bean(name = "cancelUnpaidOrderExecutor")
    public ThreadPoolTaskExecutor cancelUnpaidOrderThreadPoolConfig() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);          // 核心线程数
        executor.setMaxPoolSize(10);          // 最大线程数
        executor.setQueueCapacity(50);        // 任务队列容量
        executor.setKeepAliveSeconds(60);     // 空闲线程存活时间
        executor.setThreadNamePrefix(ThreadPoolConstant.PREFIX_BUSINESS_THREAD+THREAD_POOL_NAME); // 线程名前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略(让提交线程执行)
        executor.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(120); // 最大等待时间

        // 初始化线程池
        executor.initialize();
        return executor;
    }

}
