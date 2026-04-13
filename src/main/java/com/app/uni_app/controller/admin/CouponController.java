package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.CouponCreateDTO;
import com.app.uni_app.service.CouponService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "优惠劵管理")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;


    @PostMapping("/admin/coupon/release")
    public Result<?> saveCouponAdmin(@RequestBody @Valid @NotNull CouponCreateDTO couponCreateDTO) {
        return couponService.saveCouponAdmin(couponCreateDTO);
    }


}
