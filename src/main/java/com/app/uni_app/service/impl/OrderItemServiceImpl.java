package com.app.uni_app.service.impl;

import com.app.uni_app.mapper.OrderItemMapper;
import com.app.uni_app.pojo.entity.OrderItem;
import com.app.uni_app.service.OrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

}
