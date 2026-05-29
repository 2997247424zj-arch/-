package com.example.vuecourseproject.cashier.service;

import com.example.vuecourseproject.cashier.entity.Order;
import com.example.vuecourseproject.cashier.entity.OrderItem;

import java.util.List;

public interface OrderService {
    Order createOrder(Order order, List<OrderItem> items);
    List<Order> getOrdersByCashierName(String cashierName);
}