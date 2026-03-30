package com.app.uni_app.infrastructure.rocketmq.consumer.coupon;


import com.app.uni_app.infrastructure.rocketmq.constant.coupon.MqCouponConstant;
import com.app.uni_app.pojo.emums.CouponUseStatusEnum;
import com.app.uni_app.pojo.entity.CouponUser;
import com.app.uni_app.service.CouponUserService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RocketMQMessageListener(topic = MqCouponConstant.TOPIC_COUPON
        , consumerGroup = MqCouponConstant.CONSUMER_GROUP_COUPON_UPDATE_STATUS
        , selectorExpression = MqCouponConstant.TAG_FIXED_TIME)
@RequiredArgsConstructor
public class CouponFixedTimeStatusConsumer implements RocketMQListener<Map<String, Object>> {

    public static final String COUPON_ID = "couponId";
    public static final String COUPON_USE_STATUS_OLD = "couponUseStatusOld";
    public static final String COUPON_USE_STATUS_NEW = "couponUseStatusNew";



    private final CouponUserService couponUserService;

    @Override
    public void onMessage(Map<String, Object> messageMap) {
        Long couponId = (Long) messageMap.get(COUPON_ID);
        CouponUseStatusEnum couponUseStatusEnumOld = (CouponUseStatusEnum) messageMap.get(COUPON_USE_STATUS_OLD);
        CouponUseStatusEnum couponUseStatusEnumNew = (CouponUseStatusEnum) messageMap.get(COUPON_USE_STATUS_NEW);
        if (Objects.isNull(couponId) || Objects.isNull(couponUseStatusEnumOld) || Objects.isNull(couponUseStatusEnumNew)) {
            return;
        }
        List<CouponUser> couponUserOld = couponUserService.lambdaQuery().eq(CouponUser::getCouponId, couponId)
                .eq(CouponUser::getUseStatus, couponUseStatusEnumOld.getCode()).list();
        if (couponUserOld.isEmpty()) {
            return;
        }
        List<Long> list = couponUserOld.stream().map(CouponUser::getId).toList();
        couponUserService.lambdaUpdate().in(CouponUser::getId, list)
                .set(CouponUser::getUseStatus, couponUseStatusEnumNew.getCode()).update();
    }
}

