package com.app.uni_app.job.init;

import com.app.uni_app.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化优惠卷缓存
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CouponRedisCacheInitRunner implements ApplicationRunner {

    private final CouponService couponService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("初始化 优惠券 redis缓存...");
        couponService.updateCouponRedisCache();
        log.info("初始化 优惠券 redis缓存成功...");
    }
}
