package com.app.uni_app.infrastructure.rocketmq.consumer.coupon;

import com.app.uni_app.infrastructure.rocketmq.constant.coupon.MqCouponConstant;
import com.app.uni_app.mapper.CouponUserMapper;
import com.app.uni_app.pojo.emums.CouponUseStatusEnum;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RocketMQMessageListener(consumerGroup = MqCouponConstant.CONSUMER_GROUP_COUPON_UPDATE_STATUS
        , topic = MqCouponConstant.TOPIC_COUPON
        , selectorExpression = MqCouponConstant.TAG_AFTER_RECEIVE
        , consumeMode = ConsumeMode.CONCURRENTLY)
@RequiredArgsConstructor
public class CouponAfterReceiveStatusConsumer implements RocketMQListener<List<Map<String, Object>>> {

    public static final String COUPON_USER_ID = "couponUserId";
    public static final String COUPON_USE_STATUS_NEW = "couponUseStatusNew";

    private final CouponUserMapper couponUserMapper;

    /**
     * 批量消费 N 天后优惠券 更改状态
     * @param messageMaps 信息列表
     */
    @Override
    public void onMessage(List<Map<String, Object>> messageMaps) {
        if (messageMaps.isEmpty()) {
            return;
        }
        List<Long> couponUserIdList = new ArrayList<>(messageMaps.size());
        ArrayList<Integer> updateValueList = new ArrayList<>(messageMaps.size());
        for (Map<String, Object> messageMap : messageMaps) {
            Long couponUserId = (Long) messageMap.get(COUPON_USER_ID);
            CouponUseStatusEnum couponUseStatusEnumNew = (CouponUseStatusEnum) messageMap.get(COUPON_USE_STATUS_NEW);
            if (Objects.isNull(couponUserId) || Objects.isNull(couponUseStatusEnumNew)) {
                continue;
            }
            couponUserIdList.add(couponUserId);
            updateValueList.add(couponUseStatusEnumNew.getCode());
        }
        if (couponUserIdList.isEmpty() || updateValueList.isEmpty()) {
            return;
        }
        couponUserMapper.batchUpdateSameField(couponUserIdList, updateValueList);
    }

}
