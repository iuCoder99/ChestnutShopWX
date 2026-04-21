package com.app.uni_app.job.schedule;

import com.app.uni_app.job.constant.common.JobCommonConstant;
import com.app.uni_app.job.constant.schedule.JobScheduleConstant;
import com.app.uni_app.job.sync.product.SyncProductFromEsToRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExecutorUpdateMaxProductIdTask {



    private final ThreadPoolTaskExecutor executorSchedulerCommon;

    private final SyncProductFromEsToRedisService syncProductFromEsToRedisService;

    private static final String BUSINESS = "定时更新数据库最大商品id";


    /**
     * 定时更新数据库最大商品 ID
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void scheduledUpdateMaxProductIdInDataCache() {
        executorSchedulerCommon.execute(() -> {
            log.info(JobScheduleConstant.PREFIX_SCHEDULED_EXECUTOR_TASK + BUSINESS
                    + JobCommonConstant.THREAD_NAME +"{}"
                    +JobCommonConstant.THREAD_ID +"{}"
                    ,Thread.currentThread().getName()
                    ,Thread.currentThread().getId());
            syncProductFromEsToRedisService.syncMaxProductIdCache();
        });
    }
}
