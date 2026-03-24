package com.app.uni_app.infrastructure.thread.ThreadPoolConfig;

import com.app.uni_app.infrastructure.thread.ThreadPoolConstant.ThreadPoolConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ExecutorSchedulerConfig {
    private static final String THREAD_NAME="Async-";

    /**
     * 配置 ThreadPoolTaskExecutor 线程池
     */
    @Bean(name = "executorSchedulerCommon") // 给线程池命名，便于指定使用
    public ThreadPoolTaskExecutor executorSchedulerCommon() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);          // 核心线程数
        executor.setMaxPoolSize(20);          // 最大线程数
        executor.setQueueCapacity(50);        // 任务队列容量
        executor.setKeepAliveSeconds(60);     // 空闲线程存活时间
        executor.setThreadNamePrefix(ThreadPoolConstant.PREFIX_BUSINESS_THREAD +THREAD_NAME); // 线程名前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略(让提交线程执行)
        executor.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(120); // 最大等待时间

        // 初始化线程池
        executor.initialize();
        return executor;
    }
}