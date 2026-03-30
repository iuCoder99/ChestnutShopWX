package com.app.uni_app.mapper;

import com.app.uni_app.pojo.entity.Coupon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
    List<Coupon> selectCouponWithMutexCroupAndScopeDetail();
}
