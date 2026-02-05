package com.app.uni_app.controller.user;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.service.PayService;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@Tag(name = "支付")
@Slf4j
public class PayController {

    @Resource
    private PayService payService;

    @PostMapping("/wxpay")
    @Operation(summary = "微信支付预下单")
    public Result<PrepayWithRequestPaymentResponse> wxPay(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        log.info("微信支付预下单，订单编号: {}", orderNo);
        // PrepayWithRequestPaymentResponse response = payService.wxPay(orderNo);
        // return Result.success(response);
        return Result.success();
    }
}
