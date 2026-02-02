package com.app.uni_app.service;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

public interface PayService {
    /**
     * 微信支付预下单
     * @param orderNo 订单编号
     * @return 调起支付所需的参数
     */
    PrepayWithRequestPaymentResponse wxPay(String orderNo);
}
