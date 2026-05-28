package com.example.airplanesalehouduan.admin.dto;


import lombok.Data;

/**
 * 仪表板统计数据响应DTO
 */
@Data
public class AdminDashboardStatsResponse {
    private Long totalUsers; // 总用户数
    private Long totalOrders; // 总订单数
    private Long totalFlights; // 总航班数
    private Double totalRevenue; // 总收入
    private Long todayOrders; // 今日订单数
    private Long todayRevenue; // 今日收入
}

