package com.app.uni_app.controller.admin;

import com.app.uni_app.common.result.Result;
import com.app.uni_app.pojo.dto.OrderDTO;
import com.app.uni_app.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "提交订单信息，生成新订单")
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
    @Operation(summary = "查询订单列表", description = "分页获取订单列表，默认页码1、每页10条，默认状态为待付款（pendingPayment）")
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
    @Operation(summary = "查询订单详情", description = "根据订单编号获取订单详细信息")
    public Result getOrderDesc(@RequestParam @NotBlank String orderNo) {
        return orderService.getOrderDesc(orderNo);
    }

    /**
     * 取消订单
     * @param orderNo
     * @return
     */
    @PutMapping("/cancel")
    @Operation(summary = "取消订单", description = "根据订单编号取消订单，可选填取消原因")
    public Result cancelOrder(@RequestParam @NotBlank String orderNo, String cancelReason) {
        return orderService.cancelOrder(orderNo, cancelReason);
    }

    /**
     * 确认收货
     * @param orderNo
     * @return
     */
    @PutMapping("/confirmReceipt")
    @Operation(summary = "确认收货", description = "根据订单编号确认订单收货")
    public Result confirmOrderReceipt(@RequestParam @NotBlank String orderNo) {
        return orderService.confirmOrderReceipt(orderNo);
    }

    /**
     * 计算运费
     * @param productIds
     * @param addressId
     * @return
     */
    @GetMapping("/freight")
    @Operation(summary = "计算订单运费", description = "根据商品ID（支持批量，逗号分隔）和收货地址ID计算订单运费")
    public Result getOrderFreight(@RequestParam @NotBlank String productIds, @RequestParam @NotBlank String addressId) {
        return orderService.getOrderFreight(productIds, addressId);
    }

    /**
     * 获取物流信息
     * @param orderNo
     * @return
     */
    @GetMapping("/logistics")
    @Operation(summary = "查询物流信息", description = "根据订单编号获取订单对应的物流跟踪信息")
    public Result getOrderLogistics(@RequestParam @NotBlank String orderNo) {
        return orderService.getOrderLogistics(orderNo);
    }

}