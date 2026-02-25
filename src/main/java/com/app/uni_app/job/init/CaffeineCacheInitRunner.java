package com.app.uni_app.job.init;

import com.app.uni_app.job.schedule.ExecutorTimedTask;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CaffeineCacheInitRunner {

    @Resource
    private ExecutorTimedTask executorTimedTask;

    private static final String INIT_CAFFEINE_CACHE ="init caffeine cache...";



    @PostConstruct
    public void onApplicationEvent() {
        log.info(INIT_CAFFEINE_CACHE);

        // 初始化 Caffeine 本地缓存
        executorTimedTask.scheduledUpdateMaxProductIdInDataCache();

    }
}
