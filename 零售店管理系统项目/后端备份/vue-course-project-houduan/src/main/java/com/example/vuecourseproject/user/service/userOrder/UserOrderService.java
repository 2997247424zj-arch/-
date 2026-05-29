package com.example.vuecourseproject.user.service.userOrder;

import com.example.vuecourseproject.user.entity.userOrder.UserOrder;
import com.example.vuecourseproject.user.other.userOrder.CreateOrderDTO;
import java.util.List;

public interface UserOrderService {
    UserOrder createOrder(CreateOrderDTO orderDTO);
    List<UserOrder> getAllOrders();
    UserOrder getOrderById(Long orderId);
}