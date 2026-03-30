package com.app.uni_app.job.delay;

import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.infrastructure.rocketmq.constant.coupon.MqCouponConstant;
import com.app.uni_app.infrastructure.rocketmq.consumer.coupon.CouponAfterReceiveStatusConsumer;
import com.app.uni_app.infrastructure.rocketmq.consumer.coupon.CouponFixedTimeStatusConsumer;
import com.app.uni_app.pojo.emums.CouponUseStatusEnum;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class CouponUseStatusChangeListener {


    @Qualifier("couponStatusChangeListenerExecutor")
    @Resource
    private Executor threadPool;


    @Resource
    private RedissonClient redissonClient;


    @Resource
    private RocketMQTemplate rocketMQTemplate;


    private volatile boolean running = true;


    /**
     * 项目启动自动开启监听线程
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startListen() {
        threadPool.execute(() -> loopListen(RedisKeyGenerator.couponFixedTimeUnBeginZSet()));
        threadPool.execute(() -> loopListen(RedisKeyGenerator.couponFixedTimeInProgressZSet()));
        threadPool.execute(() -> loopListen(RedisKeyGenerator.couponAfterReceiveTimeUnBeginZSet()));
        threadPool.execute(() -> loopListen(RedisKeyGenerator.couponAfterReceiveTimeInProgressZSet()));
        running = true;
        log.info("优惠券过期监听任务 running = {}", running);
    }

    /**
     * 核心阻塞轮询
     */
    private void loopListen(String zSetKey) {
        while (running) {
            RLock lock = null;
            try {
                lock = redissonClient.getLock(getLockKey(zSetKey));
                boolean lockOk = lock.tryLock(0, 5, TimeUnit.SECONDS);
                if (!lockOk) {
                    sleep(1000);
                    continue;
                }

                long now = System.currentTimeMillis();
                //查询最早一条已过期任务
                Set<Object> taskSet = RedisConnector.opsForZSet()
                        .rangeByScore(zSetKey, 0, now, 0, 1);
                if (Objects.isNull(taskSet) || taskSet.isEmpty()) {
                    sleep(50);
                    continue;
                }
                String taskId = taskSet.toArray()[0].toString();
                doExpireBiz(taskId, zSetKey);
                RedisConnector.opsForZSet().remove(zSetKey, taskId);

            } catch (InterruptedException e) {
                log.warn("监听线程被中断，即将退出");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("延迟任务监听异常", e);
                sleep(200);
            } finally {
                unlock(lock);
            }
        }
    }

    /**
     * 业务过期逻辑
     * 移动优惠券缓存的id set
     * 使用 rocketMQ 通知
     */
    private void doExpireBiz(String taskId, String zSetKey) {
        log.info("执行过期任务 taskId:{}", taskId);
        Long couponId = Long.valueOf(taskId);
        String unBeginKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.UN_BEGIN);
        String unusedKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.UNUSED);
        String expiredKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.EXPIRED);
        //固定时间过期优惠券  未开始->未用
        if (zSetKey.equals(RedisKeyGenerator.couponFixedTimeUnBeginZSet())) {
            Set<Object> userIdSet = RedisConnector.opsForSet().members(unBeginKey);
            if (!Objects.isNull(userIdSet) && !userIdSet.isEmpty()) {
                RedisConnector.safeAddToSet(unusedKey, userIdSet.toArray(new Object[0]));
                Map<String, Object> message = createFixedTimeMqMessage(couponId, CouponUseStatusEnum.UN_BEGIN, CouponUseStatusEnum.UNUSED);
                rocketMQTemplate.convertAndSend(MqCouponConstant.TOPIC_COUPON+MqCouponConstant.TAG_FIXED_TIME,message);
            }
            RedisConnector.delete(unBeginKey);
            return;
        }

        //固定时间优惠券 未用->已过期
        if (zSetKey.equals(RedisKeyGenerator.couponFixedTimeInProgressZSet())) {
            Set<Object> userIdSet = RedisConnector.opsForSet().members(unusedKey);
            if (!Objects.isNull(userIdSet) && !userIdSet.isEmpty()) {
                RedisConnector.safeAddToSet(expiredKey, userIdSet.toArray(new Object[0]));
                Map<String, Object> message = createFixedTimeMqMessage(couponId, CouponUseStatusEnum.UNUSED, CouponUseStatusEnum.EXPIRED);
                rocketMQTemplate.convertAndSend(MqCouponConstant.TOPIC_COUPON+MqCouponConstant.TAG_FIXED_TIME,message);
            }
            RedisConnector.delete(unusedKey);
            return;
        }

        // 领券后 N 天优惠券 未开始->未用
        if (zSetKey.equals(RedisKeyGenerator.couponAfterReceiveTimeUnBeginZSet())) {
            Boolean isMember = RedisConnector.opsForSet().isMember(unBeginKey, couponId);
            if (Boolean.TRUE.equals(isMember)) {
                RedisConnector.opsForSet().remove(unBeginKey, couponId);
                RedisConnector.opsForSet().add(unusedKey, couponId);
                Map<String, Object> message = createAfterReceiveTimeMqMessage(couponId, CouponUseStatusEnum.UNUSED);
                rocketMQTemplate.convertAndSend(MqCouponConstant.TOPIC_COUPON+MqCouponConstant.TAG_AFTER_RECEIVE,message);
            }
            return;
        }
        // 领券后 N 天优惠券 未用->过期
        if (zSetKey.equals(RedisKeyGenerator.couponAfterReceiveTimeInProgressZSet())) {
            Boolean isMember = RedisConnector.opsForSet().isMember(unusedKey, couponId);
            if (Boolean.TRUE.equals(isMember)) {
                RedisConnector.opsForSet().remove(unusedKey, couponId);
                RedisConnector.opsForSet().add(expiredKey, couponId);
                Map<String, Object> message = createAfterReceiveTimeMqMessage(couponId, CouponUseStatusEnum.EXPIRED);
                rocketMQTemplate.convertAndSend(MqCouponConstant.TOPIC_COUPON+MqCouponConstant.TAG_AFTER_RECEIVE,message);
            }
        }

    }

    /**
     * 服务关闭
     */
    @PreDestroy
    public void stopListen() {
        running = false;
        log.info("Redis 延迟任务监听已停止");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    private void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * @param key 传入 ZSet 的 key
     * @return 锁对象字符串
     */
    private String getLockKey(String key) {
        return "lock:" + key;
    }

    /**
     * 创建固定时间过期的优惠券 mq 信息
     * @param couponId 优惠券 id
     * @param couponUseStatusEnumOld 用户旧的优惠券使用状态
     * @param couponUseStatusEnumNew 用户新的优惠券使用状态
     * @return 信息 map
     */
    private Map<String, Object> createFixedTimeMqMessage(Long couponId, CouponUseStatusEnum couponUseStatusEnumOld, CouponUseStatusEnum couponUseStatusEnumNew) {
        HashMap<String, Object> messageMap = new HashMap<>(3);
        messageMap.put(CouponFixedTimeStatusConsumer.COUPON_ID, couponId);
        messageMap.put(CouponFixedTimeStatusConsumer.COUPON_USE_STATUS_OLD, couponUseStatusEnumOld);
        messageMap.put(CouponFixedTimeStatusConsumer.COUPON_USE_STATUS_NEW, couponUseStatusEnumNew);
        return messageMap;
    }

    /**
     * 创建 N 天过后优惠券 mq 信息
     * @param couponUserId 用户优惠券 id
     * @param couponUseStatusEnumNew 用户新的优惠券使用状态
     * @return 信息 map
     */
    private Map<String,Object> createAfterReceiveTimeMqMessage(Long couponUserId, CouponUseStatusEnum couponUseStatusEnumNew) {
        HashMap<String, Object> messageMap = new HashMap<>(2);
        messageMap.put(CouponAfterReceiveStatusConsumer.COUPON_USER_ID,couponUserId);
        messageMap.put(CouponAfterReceiveStatusConsumer.COUPON_USE_STATUS_NEW,couponUseStatusEnumNew);
        return messageMap;
    }




}
