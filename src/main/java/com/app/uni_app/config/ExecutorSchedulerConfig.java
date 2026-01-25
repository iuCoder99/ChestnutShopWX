package com.app.uni_app.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling // 开启定时任务
@EnableAsync // 开启异步，让定时任务走自定义线程池
public class ExecutorSchedulerConfig {

    /**
     * 配置 ThreadPoolTaskExecutor 线程池
     */
    @Bean(name = "taskExecutor") // 给线程池命名，便于指定使用
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);          // 核心线程数
        executor.setMaxPoolSize(10);          // 最大线程数
        executor.setQueueCapacity(50);        // 任务队列容量
        executor.setKeepAliveSeconds(60);     // 空闲线程存活时间
        executor.setThreadNamePrefix("biz-task-"); // 线程名前缀
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略(让提交线程执行)
        executor.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(120); // 最大等待时间

        // 初始化线程池
        executor.initialize();
        return executor;
    }
}