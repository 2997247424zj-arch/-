package com.example.vuecourseproject.user.controller.userOrder;

import com.example.vuecourseproject.user.entity.userOrder.UserOrder;
import com.example.vuecourseproject.user.other.password.Result;
import com.example.vuecourseproject.user.other.userOrder.CreateOrderDTO;
import com.example.vuecourseproject.user.service.userOrder.UserOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/orders")
public class UserOrderController {

    @Autowired
    private UserOrderService orderService;

    @PostMapping
    public Result<UserOrder> createOrder(@RequestBody CreateOrderDTO orderDTO) {
        // 保持原有创建订单逻辑不变
        UserOrder order = orderService.createOrder(orderDTO);
        return Result.success(order);
    }

    @GetMapping
    public Result<List<UserOrder>> getAllOrders() {
        // 保持原有获取所有订单逻辑不变
        List<UserOrder> orders = orderService.getAllOrders();
        return Result.success(orders);
    }

    @GetMapping("/{orderId}")
    public Result<UserOrder> getOrderById(@PathVariable Long orderId) {
        // 保持原有根据ID获取订单逻辑不变
        UserOrder order = orderService.getOrderById(orderId);
        return Result.success(order);
    }
}