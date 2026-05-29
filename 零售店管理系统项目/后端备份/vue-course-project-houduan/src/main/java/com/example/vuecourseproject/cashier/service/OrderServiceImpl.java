package com.example.vuecourseproject.cashier.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.vuecourseproject.cashier.entity.Order;
import com.example.vuecourseproject.cashier.entity.OrderItem;
import com.example.vuecourseproject.cashier.mapper.OrderItemMapper;
import com.example.vuecourseproject.cashier.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional
    public Order createOrder(Order order, List<OrderItem> items) {
        orderMapper.insert(order);
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        return order;
    }

    @Override
    public List<Order> getOrdersByCashierName(String cashierName) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(Order::getCashierName, cashierName);
        return orderMapper.selectList(wrapper);
    }
}