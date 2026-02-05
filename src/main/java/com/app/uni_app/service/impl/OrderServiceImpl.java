package com.app.uni_app.service.impl;

import com.app.uni_app.aop.annotation.RemoveOrderSessionAnnotation;
import com.app.uni_app.common.constant.DataConstant;
import com.app.uni_app.common.constant.MessageConstant;
import com.app.uni_app.common.constant.RegexConstants;
import com.app.uni_app.common.context.BaseContext;
import com.app.uni_app.common.exception.PayException;
import com.app.uni_app.common.generator.SnowflakeIdGenerator;
import com.app.uni_app.common.mapstruct.CopyMapper;
import com.app.uni_app.common.result.PageResult;
import com.app.uni_app.common.result.Result;
import com.app.uni_app.common.result.ScrollQueryResult;
import com.app.uni_app.common.util.DateUtils;
import com.app.uni_app.common.util.SessionUtils;
import com.app.uni_app.mapper.OrderMapper;
import com.app.uni_app.pojo.dto.OrderDTO;
import com.app.uni_app.pojo.dto.OrderItemDTO;
import com.app.uni_app.pojo.dto.ScrollQueryDTO;
import com.app.uni_app.pojo.emums.CommonStatus;
import com.app.uni_app.pojo.emums.OrderPageEnum;
import com.app.uni_app.pojo.emums.OrderStatusEnum;
import com.app.uni_app.pojo.emums.PayTypeEnum;
import com.app.uni_app.pojo.entity.Address;
import com.app.uni_app.pojo.entity.Order;
import com.app.uni_app.pojo.entity.OrderItem;
import com.app.uni_app.pojo.vo.OrderWithItemVO;
import com.app.uni_app.pojo.vo.OrderWithTrackingVO;
import com.app.uni_app.service.AddressService;
import com.app.uni_app.service.OrderItemService;
import com.app.uni_app.service.OrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.ThreadLocalRandom;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private CopyMapper copyMapper;
    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Resource
    private OrderItemService orderItemService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private AddressService addressService;

    @Resource
    private SessionUtils sessionUtils;

    private static final String PRODUCT_IDS = "productIds";
    private static final String IS_SUCCESS = "isSuccess";
    private static final String TRY_NUM = "tryNum";


    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @RemoveOrderSessionAnnotation
    public Result insertOrder(OrderDTO orderDTO) {
        List<OrderItemDTO> orderItems = orderDTO.getOrderItems();
        if (Objects.isNull(orderItems) || orderItems.isEmpty()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        Order order = copyMapper.orderDTOToOrder(orderDTO);
        String userId = BaseContext.getUserId();
        String orderNo = snowflakeIdGenerator.generateOrderNo();
        save(order.setUserId(Long.valueOf(userId)).setOrderNo(orderNo));
        List<OrderItem> orderItemList = orderItems.stream()
                .map(orderItemDTO -> copyMapper.orderItemDTOToOrderItem(orderItemDTO))
                .peek(orderItem -> orderItem.setOrderId(order.getId())).toList();
        orderItemService.saveBatch(orderItemList);
        OrderWithItemVO orderWithItemVO = copyMapper.orderToOrderWithItemVO(order);
        return Result.success(orderWithItemVO);
    }

    /**
     * 获取订单列表(分页)
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */

    @Override
    public Result getOrderList(Integer pageNum, Integer pageSize, String status) {
        String userId = BaseContext.getUserId();
        int code = OrderStatusEnum.getByValue(status).getCode();
        IPage<Order> orderIPage = orderMapper.getOrderList(new Page<>(pageNum, pageSize), userId, code);
        List<OrderWithItemVO> orderWithItemVOS = orderIPage.getRecords().stream()
                .map(order -> copyMapper.orderToOrderWithItemVO(order)).toList();
        return Result.success(PageResult.builder().list(orderWithItemVOS).total(orderIPage.getTotal())
                .pageNum(pageNum).pageSize(pageSize).build());
    }

    /**
     * 查询指定页面订单列表
     * 查询后存入session,进行复用,一致性基于自定义注解
     * @param pageName
     * @return
     */
    @Override
    public Result getOrderListByPage(String pageName) {
        String userId = BaseContext.getUserId();
        OrderPageEnum orderPageEnum = OrderPageEnum.getByPageKey(pageName);
        List<Order> userAllOrder = sessionUtils.getUserAllOrder(orderMapper::getUserAllOrder, userId);
        if (userAllOrder.isEmpty()) {
            return Result.success();
        }
        if (orderPageEnum.equals(OrderPageEnum.ALL)) {
            List<OrderWithItemVO> orderWithItemVOs = userAllOrder.stream().map(order -> copyMapper.orderToOrderWithItemVO(order)).toList();
            return Result.success(orderWithItemVOs);
        }
        List<Order> list = userAllOrder.stream().filter(order -> order.getStatus().getPageCode() == orderPageEnum.getPageCode())
                .toList();
        List<OrderWithItemVO> orderWithItemVOs = list.stream().map(order -> copyMapper.orderToOrderWithItemVO(order)).toList();
        return Result.success(orderWithItemVOs);
    }


    /**
     * 查看订单详情
     * @param orderNo
     * @return
     */
    @Override
    public Result getOrderDesc(String orderNo) {
        Order order = orderMapper.getOrderDesc(orderNo);
        if (Objects.isNull(order)) {
            return Result.error(MessageConstant.ORDER_NOT_FOUND);
        }
        OrderWithItemVO orderWithItemVO = copyMapper.orderToOrderWithItemVO(order);
        return Result.success(orderWithItemVO);
    }

    /**
     * 取消订单
     * @param orderNo
     * @return
     */
    @Override
    @RemoveOrderSessionAnnotation
    public Result cancelOrder(String orderNo, String cancelReason) {
        String userId = BaseContext.getUserId();
        String now = DateUtils.formatLocalDateTime(LocalDateTime.now());
        boolean isSuccess = lambdaUpdate().eq(Order::getUserId, userId).eq(Order::getOrderNo, orderNo)
                .set(Order::getStatus, OrderStatusEnum.CANCELLED.getCode()).set(Order::getCancelTime, now)
                .set(Order::getCancelReason, cancelReason).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        HashMap<String, String> map = new HashMap<>(2);
        map.put(Order.Fields.orderNo, orderNo);
        map.put(Order.Fields.status, OrderStatusEnum.CANCELLED.getValue());
        return Result.success(map);
    }

    /**
     * 支付成功订单,如果五次失败,使用线程池异步进行更新(保证一定更新成功)(目前测试,只支持微信支付)
     * @param orderNo
     * @return
     */
    @Override
    @RemoveOrderSessionAnnotation
    public Result paySuccessOrder(String orderNo) {
        int tryNum = 0;
        Map<String, Object> map = new HashMap<>(2);
        updateOrderPayIsSuccess(map,orderNo, tryNum);
        boolean isSuccess = (boolean) map.get(IS_SUCCESS);
        tryNum = (int) map.get(TRY_NUM);
        while (!isSuccess) {
            Map<String, Object> nextMap = updateOrderPayIsSuccess(map,orderNo, tryNum);
            tryNum = (int) nextMap.get(TRY_NUM);
            isSuccess=(boolean) nextMap.get(IS_SUCCESS);

        }
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put(Order.Fields.orderNo, orderNo);
        resultMap.put(Order.Fields.status,OrderStatusEnum.PENDING_SHIPMENT.getValue());
        return Result.success(resultMap);
    }


    private Map<String, Object> updateOrderPayIsSuccess(Map<String,Object> map,String orderNo, int tryNum) {
        if (tryNum == 5) {
            throw new PayException(orderNo);

        }
        boolean isSuccess = lambdaUpdate().eq(Order::getOrderNo, orderNo)
                .set(Order::getStatus, OrderStatusEnum.PENDING_SHIPMENT)
                .set(Order::getPayType, PayTypeEnum.WECHAT_PAY)
                .set(Order::getPayTime,LocalDateTime.now())
                .update();
        tryNum++;
        map.put(IS_SUCCESS, isSuccess);
        map.put(TRY_NUM, tryNum);
        return map;
    }


    /**
     * 确定收货
     * @param orderNo
     * @return
     */
    @Override
    @RemoveOrderSessionAnnotation
    public Result confirmOrderReceipt(String orderNo) {
        String userId = BaseContext.getUserId();
        String now = DateUtils.formatLocalDateTime(LocalDateTime.now());
        boolean isSuccess = lambdaUpdate().eq(Order::getUserId, userId).eq(Order::getOrderNo, orderNo)
                .set(Order::getStatus, OrderStatusEnum.COMPLETED.getCode()).set(Order::getReceiveTime, now).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        HashMap<String, Object> map = new HashMap<>(3);
        map.put(Order.Fields.orderNo, orderNo);
        map.put(Order.Fields.status, OrderStatusEnum.COMPLETED.getValue());
        map.put(Order.Fields.receiveTime, now);
        return Result.success(map);
    }


    /**
     * 删除订单
     * @param orderNo
     * @return
     */
    @Override
    public Result deleteOrder(String orderNo) {
        boolean isSuccess = lambdaUpdate().set(Order::getIs_deleted, CommonStatus.ACTIVE.getNumber()).eq(Order::getOrderNo, orderNo).update();
        if (!isSuccess) {
            return Result.error(MessageConstant.DELETE_ERROR);
        }
        return Result.success(orderNo);
    }

    /**
     * 计算运费
     * @param productIds
     * @param addressId
     * @return
     */
    @Override
    public Result getOrderFreight(String productIds, String addressId) {
        String[] productIdsArray = StringUtils.split(productIds, ",");
        Address address = addressService.getById(addressId);
        if (Objects.isNull(address)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        /*
          TODO 计算运费相关逻辑 快递鸟API
         */
        BigDecimal originalFreight = BigDecimal.valueOf(productIdsArray.length * ThreadLocalRandom.current().nextDouble(3, 8));
        double freight = originalFreight.setScale(2, RoundingMode.HALF_UP).doubleValue();
        HashMap<String, Object> map = new HashMap<>(2);
        map.put(Order.Fields.freight, freight);
        map.put(PRODUCT_IDS, productIdsArray);
        return Result.success(map);
    }

    /**
     * 获取物流信息
     * @param orderNo
     * @return
     */
    @Override
    public Result getOrderLogistics(String orderNo) {
        Order order = orderMapper.getOrderLogistics(orderNo);
        if (Objects.isNull(order)) {
            return Result.error(MessageConstant.ORDER_NOT_FOUND);
        }
        OrderWithTrackingVO orderWithTrackingVO = copyMapper.orderToOrderWithTrackingVO(order);
        return Result.success(orderWithTrackingVO);
    }

    /**
     * 滚动游标查询订单
     * @return
     */
    @Override
    public Result getOrderByScrollQuery(ScrollQueryDTO scrollQueryDTO) {
        String userId = BaseContext.getUserId();
        List<Order> list;
        Long beginId = scrollQueryDTO.getBeginId();
        if (Objects.isNull(beginId)) {
            list = lambdaQuery().eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime)
                    .eq(Order::getIs_deleted, CommonStatus.INACTIVE.getNumber())
                    .last("LIMIT " + DataConstant.COMMON_SCROLL_QUERY_NUMBER)
                    .list();
        } else {
            list = lambdaQuery().eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime).lt(Order::getId, beginId)
                    .eq(Order::getIs_deleted, CommonStatus.INACTIVE.getNumber())
                    .last("LIMIT " + DataConstant.COMMON_SCROLL_QUERY_NUMBER)
                    .list();
        }
        if (list.isEmpty()) {
            return Result.success(ScrollQueryResult.builder().list(list).endId(beginId));
        }
        Long endId = list.get(list.size() - 1).getId();
        return Result.success(ScrollQueryResult.builder().list(list).endId(endId).build());

    }

    /**
     * 条件搜索订单
     * @param searchCondition
     * @return
     */
    @Override
    public Result searchOrderByCondition(String searchCondition) {
        String userId = BaseContext.getUserId();
        String orderNo = null;
        String logisticsNo = null;
        String productName = null;
        if (RegexConstants.isOrderNo(searchCondition)) {
            orderNo = searchCondition;
        } else if (RegexConstants.isLogisticsNo(searchCondition)) {
            logisticsNo = searchCondition;
        } else {
            productName = searchCondition;

        }
        List<Order> orderList = orderMapper.searchOrderByCondition(orderNo, logisticsNo, productName, userId);
        if (orderList.isEmpty()) {
            return Result.success();

        }
        List<OrderWithItemVO> orderWithItemVOs = orderList.stream().map(order -> copyMapper.orderToOrderWithItemVO(order)).toList();
        return Result.success(orderWithItemVOs);
    }
}
