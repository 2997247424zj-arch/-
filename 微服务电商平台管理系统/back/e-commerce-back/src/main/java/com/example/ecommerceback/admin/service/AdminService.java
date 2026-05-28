package com.example.ecommerceback.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.ecommerceback.order.entity.Order;
import com.example.ecommerceback.order.mapper.OrderMapper;
import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.mapper.ProductMapper;
import com.example.ecommerceback.user.entity.User;
import com.example.ecommerceback.user.mapper.UserMapper;
import com.example.ecommerceback.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;

    public Result<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("userCount", userMapper.selectCount(null));
            stats.put("productCount", productMapper.selectCount(null));
            stats.put("orderCount", orderMapper.selectCount(null));
            stats.put("totalRevenue", calculateTotalRevenue());
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(500, "获取统计数据失败: " + e.getMessage());
        }
    }

    public Result<List<Map<String, Object>>> getRecentActivities() {
        try {
            List<Map<String, Object>> activities = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            List<User> latestUsers = userMapper.selectList(
                new QueryWrapper<User>().orderByDesc("create_time").last("LIMIT 3")
            );
            latestUsers.forEach(user -> activities.add(activity(
                formatter,
                user.getCreateTime(),
                "用户 " + user.getUsername() + " 完成注册",
                "user"
            )));

            List<Order> latestOrders = orderMapper.selectList(
                new QueryWrapper<Order>().orderByDesc("create_time").last("LIMIT 3")
            );
            latestOrders.forEach(order -> activities.add(activity(
                formatter,
                order.getCreateTime(),
                "订单 " + order.getOrderNo() + " 已创建",
                "order"
            )));

            List<Product> latestProducts = productMapper.selectList(
                new QueryWrapper<Product>().orderByDesc("create_time").last("LIMIT 3")
            );
            latestProducts.forEach(product -> activities.add(activity(
                formatter,
                product.getCreateTime(),
                "商品 " + product.getName() + " 已更新",
                "product"
            )));

            activities.sort(Comparator.comparing(activity -> String.valueOf(activity.get("timestamp")), Comparator.reverseOrder()));
            return Result.success(activities.stream().limit(6).toList());
        } catch (Exception e) {
            return Result.error(500, "获取活动数据失败: " + e.getMessage());
        }
    }

    private BigDecimal calculateTotalRevenue() {
        try {
            List<Order> paidOrders = orderMapper.selectList(
                new QueryWrapper<Order>().in("status", 1, 3)
            );

            return paidOrders.stream()
                .map(order -> BigDecimal.valueOf(
                    order.getPayAmount() != null
                        ? order.getPayAmount()
                        : (order.getTotalAmount() != null ? order.getTotalAmount() : 0D)
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Map<String, Object> activity(DateTimeFormatter formatter, LocalDateTime time, String content, String type) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("timestamp", (time == null ? LocalDateTime.now() : time).format(formatter));
        activity.put("content", content);
        activity.put("type", type);
        return activity;
    }
}
