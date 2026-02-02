package com.app.uni_app.service.impl;

import com.app.uni_app.mapper.OrderMapper;
import com.app.uni_app.pojo.entity.Order;
import com.app.uni_app.pojo.entity.SysUser;
import com.app.uni_app.properties.WeChatProperties;
import com.app.uni_app.service.PayService;
import com.app.uni_app.service.UserService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class PayServiceImpl implements PayService {

    @Resource
    private WeChatProperties weChatProperties;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserService userService;

    @Override
    public PrepayWithRequestPaymentResponse wxPay(String orderNo) {
        // 1. 查询订单信息
        Order order = orderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 2. 查询用户信息（获取 openid）
        SysUser user = userService.getById(order.getUserId());
        if (user == null || user.getOpenid() == null) {
            throw new RuntimeException("用户信息异常或 openid 为空");
        }

        // 3. 配置微信支付
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(weChatProperties.getMchid())
                .privateKeyFromPath(weChatProperties.getPrivateKeyFilePath())
                .merchantSerialNumber(weChatProperties.getMchSerialNo())
                .apiV3Key(weChatProperties.getApiV3Key())
                .build();

        // 4. 初始化 JsapiServiceExtension
        JsapiServiceExtension service = new JsapiServiceExtension.Builder()
                .config(config)
                .build();

        // 5. 构造请求体
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        // 微信支付单位为分
        int total = order.getTotalAmount().multiply(new BigDecimal("100")).intValue();
        amount.setTotal(total);
        request.setAmount(amount);
        request.setAppid(weChatProperties.getAppid());
        request.setMchid(weChatProperties.getMchid());
        request.setDescription("订单支付-" + order.getOrderNo());
        request.setNotifyUrl(weChatProperties.getNotifyUrl());
        request.setOutTradeNo(order.getOrderNo());

        Payer payer = new Payer();
        payer.setOpenid(user.getOpenid());
        request.setPayer(payer);

        // 6. 调用预下单接口并自动生成前端调起支付所需的签名参数
        log.info("发起微信预支付请求: {}", order.getOrderNo());
        return service.prepayWithRequestPayment(request);
    }
}
