package com.app.uni_app.service;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.CouponCreateDTO;
import com.app.uni_app.pojo.entity.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface CouponService extends IService<Coupon> {

    Result<?> saveCouponAdmin(@Valid @NotNull CouponCreateDTO couponCreateDTO);

    void updateCouponRedisCache();
}
