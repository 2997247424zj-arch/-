package com.example.ecommerceback.order.controller;

import com.example.ecommerceback.order.service.OrderService;
import com.example.ecommerceback.utils.Result;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.example.ecommerceback.order.entity.Order;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<String> create(HttpSession session, @RequestBody Map<String, Object> data) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Long addressId = Long.valueOf(data.get("addressId").toString());
        return orderService.create(userId, addressId);
    }

    @GetMapping("/list")
    public Result<List<Order>> list(HttpSession session, @RequestParam(required = false) Map<String, Object> params) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return orderService.list(userId);
    }

    /**
     * 获取当前用户的订单列表（用于个人数据看板）
     */
    @GetMapping("/my-list")
    public Result<List<Order>> myList(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/detail/{orderNo}")
    public Result<Object> detail(HttpSession session, @PathVariable String orderNo) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return orderService.detail(userId, orderNo);
    }

    @PostMapping("/cancel")
    public Result<Void> cancel(HttpSession session, @RequestBody Map<String, String> data) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        String orderNo = data.get("orderNo");
        return orderService.cancel(userId, orderNo);
    }

    @PostMapping("/confirm-receive")
    public Result<String> confirmReceive(HttpSession session, @RequestBody Map<String, String> data) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        String orderNo = data.get("orderNo");
        return orderService.confirmReceive(userId, orderNo);
    }
}
