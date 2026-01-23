package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.OrderDTO;
import com.app.uni_app.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping("/create")
    public Result insertOrder(@RequestBody @NotNull OrderDTO orderDTO) {
        return orderService.insertOrder(orderDTO);
    }

    /**
     * 获取订单列表
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") Integer pageNum
            , @RequestParam(defaultValue = "10") Integer pageSize
            , @RequestParam(defaultValue = "pendingPayment") String status) {
        return orderService.getOrderList(pageNum, pageSize, status);
    }

    /**
     * 查看订单详情
     * @param orderNo
     * @return
     */
    @GetMapping("/detail")
    public Result getOrderDesc(@RequestParam @NotBlank String orderNo) {
        return orderService.getOrderDesc(orderNo);
    }

    /**
     * 取消订单
     * @param orderNo
     * @return
     */
    @PutMapping("/cancel")
    public Result cancelOrder(@RequestParam @NotBlank String orderNo, String cancelReason) {
        return orderService.cancelOrder(orderNo, cancelReason);
    }


    /**
     * 确认收货
     * @param orderNo
     * @return
     */
    @PutMapping("/confirmReceipt")
    public Result confirmOrderReceipt(@RequestParam @NotBlank String orderNo) {
        return orderService.confirmOrderReceipt(orderNo);
    }

    /**
     * 计算运费
     * @param productIds
     * @param address
     * @return
     */
    @GetMapping("/freight")
    public Result getOrderFreight(@RequestParam @NotBlank String productIds, @RequestParam @NotBlank String addressId) {
        return orderService.getOrderFreight(productIds, addressId);
    }


    /**
     * 获取物流信息
     * @param orderNo
     * @return
     */
    @GetMapping("/logistics")
    public Result getOrderLogistics(@RequestParam @NotBlank String orderNo) {
        return orderService.getOrderLogistics(orderNo);
    }


}
