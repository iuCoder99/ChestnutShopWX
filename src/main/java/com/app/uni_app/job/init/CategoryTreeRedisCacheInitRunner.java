package com.app.uni_app.job.init;

import com.app.uni_app.service.CategoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CategoryTreeRedisCacheInitRunner  implements ApplicationRunner {

    @Resource
    private CategoryService categoryService;


    @Override
    public void run(ApplicationArguments args) {
        log.info("初始化 分类树 服务器缓存...");
        categoryService.updateCategoryTreeRedisCache();
        log.info("初始化 分类树 服务器缓存成功...");
    }


}
