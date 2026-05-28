package com.example.airplanesalehouduan.passengers.controller;



import com.example.airplanesalehouduan.Login.dto.ApiResponse;

import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * 提供普通乘客的订单查询接口
 * 实现数据隔离，确保用户只能查询自己的订单
 *
 * 注意：本系统使用基于会话的认证方式
 * 前端需要在请求中传递乘客ID（从sessionStorage中获取）
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取我的订单列表（分页）
     * 接口路径：GET /api/orders
     * 请求参数：
     *   - passengerId: 乘客ID（必需，从sessionStorage中获取）
     *   - page: 页码（可选，默认0）
     *   - size: 每页大小（可选，默认10）
     *   - orderNumber: 订单号（可选，模糊查询）
     *   - customer: 客户姓名（可选，模糊查询）
     *   - status: 订单状态（可选，如：created, paid, ticketed, cancelled, refunded）
     *   - startDate: 开始日期（可选，格式：yyyy-MM-dd）
     *   - endDate: 结束日期（可选，格式：yyyy-MM-dd）
     *
     * @param passengerId 乘客ID（从请求参数获取）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认10）
     * @param orderNumber 订单号（可选）
     * @param customer 客户姓名（可选）
     * @param status 订单状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 订单列表和分页信息
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<?>> getOrders(
            @RequestParam(required = true) Integer passengerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            Page<Order> orderPage = orderService.getOrdersByPassengerIdWithFilters(
                    passengerId, page, size, orderNumber, customer, status, startDate, endDate);

            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orders", orderPage.getContent());
            responseData.put("total", orderPage.getTotalElements());
            responseData.put("page", orderPage.getNumber());
            responseData.put("size", orderPage.getSize());
            responseData.put("totalPages", orderPage.getTotalPages());
            responseData.put("hasNext", orderPage.hasNext());
            responseData.put("hasPrevious", orderPage.hasPrevious());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询订单失败: " + e.getMessage()));
        }
    }

    /**
     * 获取订单详情（根据订单ID）
     * 接口路径：GET /api/orders/{orderId}
     *
     * @param orderId 订单ID
     * @param passengerId 乘客ID（从请求参数获取，用于验证权限）
     * @return 订单详情
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrderDetail(
            @PathVariable Long orderId,
            @RequestParam(required = true) Integer passengerId) {
        try {
            Order order = orderService.getOrderById(passengerId, orderId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取订单详情（根据订单号）
     * 接口路径：GET /api/orders/by-order-no/{orderNo}
     *
     * @param orderNo 订单号
     * @param passengerId 乘客ID（从请求参数获取，用于验证权限）
     * @return 订单详情
     */
    @GetMapping("/orders/by-order-no/{orderNo}")
    public ResponseEntity<ApiResponse<?>> getOrderDetailByOrderNo(
            @PathVariable String orderNo,
            @RequestParam(required = true) Integer passengerId) {
        try {
            Order order = orderService.getOrderByOrderNo(passengerId, orderNo);
            return ResponseEntity.ok(ApiResponse.success("查询成功", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取最近/快捷订单（快捷接口）
     * 接口路径：GET /api/passenger/orders/recent
     * 用于乘客仪表板或快捷入口显示近期的订单。为了支持退订/改签快捷入口展示所有“待出行”订单：
     *  - 默认返回状态为“待出行”的订单
     *  - 默认limit扩大到100以覆盖更多数目的场景
     *
     * @param passengerId 乘客ID（从请求参数获取）
     * @param limit 限制数量（可选，默认100；传0或负数表示返回所有匹配条目）
     * @param status 订单状态（可选，默认 '待出行'）
     * @return 最近订单列表
     */
    @GetMapping("/passenger/orders/recent")
    public ResponseEntity<ApiResponse<?>> getRecentOrders(
            @RequestParam(required = true) Integer passengerId,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String status) {
        try {
            String queryStatus = (status == null || status.trim().isEmpty()) ? "待出行" : status.trim();

            // 如果limit <= 0，视为请求返回所有匹配的结果（按页查询直到取完）
            List<Order> orders;
            if (limit <= 0) {
                orders = new ArrayList<>();
                int page = 0;
                int pageSize = 100;
                while (true) {
                    Page<Order> pageResult = orderService.getOrdersByPassengerIdAndStatus(passengerId, queryStatus, page, pageSize);
                    if (pageResult == null || pageResult.getContent().isEmpty()) break;
                    orders.addAll(pageResult.getContent());
                    if (!pageResult.hasNext()) break;
                    page++;
                }
            } else {
                Page<Order> pageResult = orderService.getOrdersByPassengerIdAndStatus(passengerId, queryStatus, 0, limit);
                orders = pageResult != null ? pageResult.getContent() : new ArrayList<>();
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orders", orders);
            responseData.put("count", orders.size());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询最近订单失败: " + e.getMessage()));
        }
    }

    /**
     * 获取订单统计信息
     * 接口路径：GET /api/passenger/orders/statistics
     *
     * @param passengerId 乘客ID（从请求参数获取）
     * @return 订单统计信息
     */
    @GetMapping("/passenger/orders/statistics")
    public ResponseEntity<ApiResponse<?>> getOrderStatistics(
            @RequestParam(required = true) Integer passengerId) {
        try {
            Map<String, Object> statistics = orderService.getOrderStatistics(passengerId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", statistics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询订单统计失败: " + e.getMessage()));
        }
    }

    /**
     * 删除订单
     * 接口路径：DELETE /api/orders/{orderId}
     *
     * @param orderId 订单ID
     * @param passengerId 乘客ID（从请求参数获取，用于验证权限）
     * @return 删除结果
     */
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<?>> deleteOrder(
            @PathVariable Long orderId,
            @RequestParam(required = true) Integer passengerId) {
        try {
            orderService.deleteOrder(passengerId, orderId);
            return ResponseEntity.ok(ApiResponse.success("订单删除成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

