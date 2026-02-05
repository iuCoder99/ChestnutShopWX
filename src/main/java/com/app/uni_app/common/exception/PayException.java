package com.app.uni_app.common.exception;


import com.app.uni_app.infrastructure.thread.ThreadPoolConstant.ThreadPoolConstant;
import com.app.uni_app.pojo.emums.OrderStatusEnum;
import com.app.uni_app.pojo.entity.Order;
import com.app.uni_app.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;

@Slf4j
public class PayException extends RuntimeException {

    @Resource
    @Qualifier("solvePayExceptionThreadPool")
    private Executor solvePayExceptionThreadPool;

    @Resource
    private OrderService orderService;
    private static final String ORDER_NO = "订单: ";
    private static final String TASK_NAME = " | 处理订单支付成功,数据库确认失败";

    /**
     * 处理 支付成功,但程序确认失败的订单号
     */
    public PayException(String orderNo) {
        super();
        solvePaySuccessButConfirmFail(orderNo);
    }

    public void solvePaySuccessButConfirmFail(String orderNo) {
        solvePayExceptionThreadPool.execute(() -> {
            log.error(ORDER_NO + "{}" +
                            TASK_NAME + ThreadPoolConstant.PREFIX_TASK + "{}"
                            + ThreadPoolConstant.THREAD_NAME + "{}"
                            + ThreadPoolConstant.THREAD_ID + "{}"
                    , orderNo
                    , LocalDateTime.now()
                    , Thread.currentThread().getName()
                    , Thread.currentThread().getId());
            boolean isSuccess = orderService.lambdaUpdate().eq(Order::getOrderNo, orderNo)
                    .set(Order::getStatus, OrderStatusEnum.PENDING_CONFIRM)
                    .update();
            if (!isSuccess) {
                solvePaySuccessButConfirmFail(orderNo);

            }
        });
    }
}
