package com.app.uni_app.service.impl;

import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.infrastructure.redis.connect.RedisConnector;
import com.app.uni_app.infrastructure.redis.generator.RedisKeyGenerator;
import com.app.uni_app.mapper.CouponMapper;
import com.app.uni_app.pojo.dto.CouponCreateDTO;
import com.app.uni_app.pojo.emums.CouponUseStatusEnum;
import com.app.uni_app.pojo.emums.CouponValidModeEnum;
import com.app.uni_app.pojo.entity.Coupon;
import com.app.uni_app.pojo.entity.CouponUser;
import com.app.uni_app.service.CouponService;
import com.app.uni_app.service.CouponUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CopyMapper copyMapper;
    private final CouponMapper couponMapper;
    private final CouponUserService couponUserService;

    @Override
    public Result<?> saveCouponAdmin(CouponCreateDTO couponCreateDTO) {
        Coupon coupon = copyMapper.couponCreateDTOToCoupon(couponCreateDTO);
        boolean isSuccess = save(coupon);
        if (!isSuccess) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        return Result.success();
    }

    /**
     * 初始化更新购物券缓存
     */
    @Override
    public void updateCouponRedisCache() {
        List<Coupon> couponList = couponMapper.selectCouponWithMutexCroupAndScopeDetail();
        RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                for (Coupon coupon : couponList) {
                    String key = RedisKeyGenerator.couponDetail(coupon.getId());
                    RedisConnector.setHashObject(key, coupon);
                }
                return null;
            }
        });
        List<Long> couponIdList = couponList.stream().map(Coupon::getId).toList();
        if (couponIdList.isEmpty()) {
            return;
        }
        Map<Long, List<CouponUser>> couponUserMap = couponUserService.lambdaQuery()
                .in(CouponUser::getCouponId, couponIdList).list()
                .stream().collect(Collectors.groupingBy(CouponUser::getCouponId));
        couponList.forEach(coupon -> coupon.setCouponUserList(couponUserMap.get(coupon.getId())));
        List<Coupon> couponFixedTimeList = couponList.stream()
                .filter(coupon -> Objects.equals(coupon.getValidMode(), CouponValidModeEnum.FIXED_TIME.getCode()))
                .toList();
        updateCouponFixedTimeListCache(couponFixedTimeList);
        List<Coupon> couponAfterReceiveList = couponList.stream().filter(coupon -> Objects.equals(coupon.getValidMode(), CouponValidModeEnum.AFTER_RECEIVE.getCode()))
                .toList();
        updateAfterReceiveListCache(couponAfterReceiveList);
    }

    /**
     * 更新固定时间优惠券缓存
     * 维护 ZSet 存储优惠券id 监控状态变化 , 维护 Set 存储用户 id
     * @param couponList 固定时间优惠券列表
     */
    private void updateCouponFixedTimeListCache(List<Coupon> couponList) {
        if (couponList == null || couponList.isEmpty()) {
            return;
        }
        for (Coupon coupon : couponList) {
            LocalDateTime validStart = coupon.getValidStart();
            LocalDateTime validEnd = coupon.getValidEnd();
            LocalDateTime now = LocalDateTime.now();
            Long couponId = coupon.getId();
            List<CouponUser> couponUserList = coupon.getCouponUserList();

            if (now.isBefore(validStart)) {
                long timestamp = validStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                String key = RedisKeyGenerator.couponFixedTimeUnBeginZSet();
                RedisConnector.opsForZSet().add(key, couponId, timestamp);
                String userIdListSetKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.UN_BEGIN);
                RedisConnector.safeAddToSet(userIdListSetKey, couponUserList.stream()
                        .map(CouponUser::getUserId).distinct().toArray(Object[]::new));
            } else if (now.isAfter(validEnd)) {
                HashSet<Long> used = new HashSet<>();
                HashSet<Long> expired = new HashSet<>();
                for (CouponUser couponUser : couponUserList) {
                    switch (CouponUseStatusEnum.getByCode(couponUser.getUseStatus())) {
                        case UN_BEGIN, EXPIRED, RETURNED -> expired.add(couponUser.getUserId());
                        case USED -> used.add(couponUser.getUserId());
                    }
                }
                String expireKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.EXPIRED);
                String usedKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.USED);

                RedisConnector.safeAddToSet(expireKey, expired.toArray(new Object[0]));
                RedisConnector.safeAddToSet(usedKey, used.toArray(new Object[0]));
            } else {
                long timestamp = validEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                String key = RedisKeyGenerator.couponFixedTimeInProgressZSet();
                RedisConnector.opsForZSet().add(key, couponId, timestamp);
                Set<Long> unused = new HashSet<>();
                Set<Long> used = new HashSet<>();
                Set<Long> returned = new HashSet<>();
                for (CouponUser couponUser : couponUserList) {
                    switch (CouponUseStatusEnum.getByCode(couponUser.getUseStatus())) {
                        case UN_BEGIN, UNUSED -> unused.add(couponUser.getUserId());
                        case USED -> used.add(couponUser.getUserId());
                        case RETURNED -> returned.add(couponUser.getUserId());
                    }
                }
                String unusedKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.UNUSED);
                String usedKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.USED);
                String returnedKey = RedisKeyGenerator.couponUseStatusIdSet(couponId, CouponUseStatusEnum.RETURNED);

                RedisConnector.safeAddToSet(unusedKey, unused.toArray(new Object[0]));
                RedisConnector.safeAddToSet(returnedKey, returned.toArray(new Object[0]));
                RedisConnector.safeAddToSet(usedKey, used.toArray(new Object[0]));
            }
        }
    }

    /**
     * 更新领劵后 N 天 优惠券缓存
     * 维护 ZSet (其中存储 couponUserId)监控状态变化 和 Set 存储用户 id
     * @param couponList 领券后 N 天优惠券列表
     */
    private void updateAfterReceiveListCache(List<Coupon> couponList) {
        filterCouponUserStatus(couponList, CouponUseStatusEnum.USED);
        filterCouponUserStatus(couponList, CouponUseStatusEnum.RETURNED);
        List<Coupon> userStatusFilterList = couponList.stream().peek(coupon -> {
            List<CouponUser> couponUserList = coupon.getCouponUserList().stream()
                    .filter(couponUser -> couponUser.getUseStatus().equals(CouponUseStatusEnum.UN_BEGIN.getCode())
                            || couponUser.getUseStatus().equals(CouponUseStatusEnum.UNUSED.getCode())
                            || couponUser.getUseStatus().equals(CouponUseStatusEnum.EXPIRED.getCode())).toList();
            coupon.setCouponUserList(couponUserList);
        }).toList();

        RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                for (Coupon coupon : userStatusFilterList) {
                    HashSet<Long> unused = new HashSet<>();
                    HashSet<Long> unBegin = new HashSet<>();
                    HashSet<Long> expired = new HashSet<>();
                    for (CouponUser couponUser : coupon.getCouponUserList()) {
                        switch (CouponUseStatusEnum.getByCode(coupon.getStatus())) {
                            case UN_BEGIN -> {
                                unBegin.add(couponUser.getUserId());
                                updateCouponUserRedisCache(couponUser);
                            }
                            case UNUSED -> {
                                unused.add(couponUser.getUserId());
                                updateCouponUserRedisCache(couponUser);
                            }
                            default -> expired.add(couponUser.getUserId());
                        }
                    }
                    RedisConnector.safeAddToSet(RedisKeyGenerator.couponUseStatusIdSet(coupon.getId(), CouponUseStatusEnum.UNUSED), unused.toArray(new Object[0]));
                    RedisConnector.safeAddToSet(RedisKeyGenerator.couponUseStatusIdSet(coupon.getId(), CouponUseStatusEnum.UN_BEGIN), unBegin.toArray(new Object[0]));
                    RedisConnector.safeAddToSet(RedisKeyGenerator.couponUseStatusIdSet(coupon.getId(), CouponUseStatusEnum.EXPIRED), expired.toArray(new Object[0]));
                }
                return null;
            }
        });
    }

    private static void filterCouponUserStatus(List<Coupon> couponList, CouponUseStatusEnum couponUseStatusEnum) {
        List<Coupon> usedList = couponList.stream().peek(coupon -> {
            List<CouponUser> couponUserList = coupon.getCouponUserList().stream()
                    .filter(couponUser -> couponUser.getUseStatus().equals(couponUseStatusEnum.getCode())).toList();
            coupon.setCouponUserList(couponUserList);
        }).toList();

        RedisConnector.executePipelined(new SessionCallback<>() {
            @Override
            public <K, V> Object execute(@Nullable RedisOperations<K, V> operations) throws DataAccessException {
                for (Coupon coupon : usedList) {
                    Long couponId = coupon.getId();
                    String key = RedisKeyGenerator.couponUseStatusIdSet(couponId, couponUseStatusEnum);

                    RedisConnector.safeAddToSet(key, coupon.getCouponUserList().stream()
                            .map(CouponUser::getUserId).distinct().toArray(Object[]::new));
                }
                return null;
            }
        });
    }

    /**
     * 更新 N 天后过期的优惠券的 ZSet 缓存 其中存储couponUserId
     * @param couponUser 用户持有的优惠券
     */
    private void updateCouponUserRedisCache(CouponUser couponUser) {
        Long couponUserId = couponUser.getCouponId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = couponUser.getValidStart();
        LocalDateTime end = couponUser.getValidEnd();
        if (now.isBefore(start)) {
            String key = RedisKeyGenerator.couponAfterReceiveTimeUnBeginZSet();
            long timestamp = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            RedisConnector.opsForZSet().add(key, couponUserId, timestamp);
        }
        if (now.isAfter(start) && now.isBefore(end)) {
            String key = RedisKeyGenerator.couponAfterReceiveTimeInProgressZSet();
            long timestamp = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            RedisConnector.opsForZSet().add(key, couponUserId, timestamp);
        }
    }


}