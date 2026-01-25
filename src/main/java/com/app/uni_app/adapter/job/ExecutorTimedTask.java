package com.app.uni_app.adapter.job;

import com.app.uni_app.common.util.CaffeineUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ExecutorTimedTask {

    @Resource
    private CaffeineUtils caffeineUtils;

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
        scheduledUpdateMaxProductIdInDataCache();
    }

    @Async("taskExecutor") // 关键：绑定自定义线程池
    @Scheduled(cron = "0 0 0/1 * * ?")  // 每小时0分0秒执行（如00:00、01:00、02:00...）
    public void scheduledUpdateMaxProductIdInDataCache() {
        System.out.println(PREFIX_SCHEDULED_EXECUTOR_TASK + LocalDateTime.now()
                + THREAD_NAME + Thread.currentThread().getName()
                + THREAD_ID + Thread.currentThread().getId());
        caffeineUtils.updateMaxProductIdInData();

    }
}