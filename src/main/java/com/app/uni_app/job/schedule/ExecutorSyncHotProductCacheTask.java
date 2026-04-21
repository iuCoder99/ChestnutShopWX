package com.app.uni_app.job.schedule;


import com.app.uni_app.job.constant.common.JobCommonConstant;
import com.app.uni_app.job.constant.schedule.JobScheduleConstant;
import com.app.uni_app.job.sync.product.impl.SyncProductFromEsToRedisServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutorSyncHotProductCacheTask {

    private final SyncProductFromEsToRedisServiceImpl syncProductFromEsToRedisServiceImpl;

    private final ThreadPoolTaskExecutor executorSchedulerCommon;

    private static final String BUSINESS = "同步热门商品数据定时任务执行";

    @Scheduled(cron = "0 0 0/12 * * ?")
    public void syncHotProductRedisCache() {
        executorSchedulerCommon.execute(()->{
            log.info(JobScheduleConstant.PREFIX_SCHEDULED_EXECUTOR_TASK
                            + BUSINESS
                            + JobCommonConstant.THREAD_NAME + "{}"
                            + JobCommonConstant.THREAD_ID + "{}"
                    , Thread.currentThread().getName()
                    , Thread.currentThread().getId());
            syncProductFromEsToRedisServiceImpl.syncHotProductCache();
        });
    }
}
