package com.app.uni_app.job.schedule;

import com.app.uni_app.common.util.CaffeineUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExecutorTimedTask {


    private final CaffeineUtils caffeineUtils;

    private final ThreadPoolTaskExecutor executorSchedulerCommon;


    private static final String PREFIX_SCHEDULED_EXECUTOR_TASK = "定时任务执行：";
    private static final String THREAD_NAME = " | 线程名: ";
    private static final String THREAD_ID = " | 线程ID: ";


    /**
     * 定时更新数据库最大商品 ID
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void scheduledUpdateMaxProductIdInDataCache() {
        executorSchedulerCommon.execute(() -> {
            System.out.println(PREFIX_SCHEDULED_EXECUTOR_TASK + LocalDateTime.now()
                    + THREAD_NAME + Thread.currentThread().getName()
                    + THREAD_ID + Thread.currentThread().getId());
            caffeineUtils.updateMaxAndMinProductIdInData();
        });
    }
}
