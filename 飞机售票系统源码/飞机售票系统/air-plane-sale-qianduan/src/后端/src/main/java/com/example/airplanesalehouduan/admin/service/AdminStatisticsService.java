package com.example.airplanesalehouduan.admin.service;

import com.example.airplanesalehouduan.admin.entity.AdminOrder;
import com.example.airplanesalehouduan.admin.repository.AdminOrderRepository;
import com.example.airplanesalehouduan.Login.repository.UserRepository;
import com.example.airplanesalehouduan.admin.repository.AdminFlightRepository;
import com.example.airplanesalehouduan.admin.dto.AdminDashboardStatsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.airplanesalehouduan.passengers.other.TicketCancelRequestRepository;
import com.example.airplanesalehouduan.passengers.other.TicketChangeRequestRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 管理员统计服务
 */
@Service
public class AdminStatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminOrderRepository adminOrderRepository;

    @Autowired
    private AdminFlightRepository adminFlightRepository;

    @Autowired
    private TicketCancelRequestRepository ticketCancelRequestRepository;

    @Autowired
    private TicketChangeRequestRepository ticketChangeRequestRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * 获取仪表板统计数据
     */
    public AdminDashboardStatsResponse getDashboardStats() {
        AdminDashboardStatsResponse stats = new AdminDashboardStatsResponse();

        // 总用户数
        stats.setTotalUsers(userRepository.count());

        // 总订单数
        stats.setTotalOrders(adminOrderRepository.count());

        // 总航班数
        stats.setTotalFlights(adminFlightRepository.count());

        // 总收入（所有订单的总金额）
        BigDecimal totalRevenue = adminOrderRepository.findAll().stream()
                .map(AdminOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalRevenue(totalRevenue.doubleValue());

        // 今日订单数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long todayOrders = adminOrderRepository.findAll().stream()
                .filter(AdminOrder -> {
                    LocalDateTime createdAt = AdminOrder.getCreatedAt();
                    return createdAt != null &&
                            createdAt.isAfter(todayStart) &&
                            createdAt.isBefore(todayEnd);
                })
                .count();
        stats.setTodayOrders(todayOrders);

        // 今日收入
        BigDecimal todayRevenue = adminOrderRepository.findAll().stream()
                .filter(AdminOrder -> {
                    LocalDateTime createdAt = AdminOrder.getCreatedAt();
                    return createdAt != null &&
                            createdAt.isAfter(todayStart) &&
                            createdAt.isBefore(todayEnd);
                })
                .map(AdminOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodayRevenue(todayRevenue.longValue());

        return stats;
    }

    /**
     * 获取订单趋势（按天统计近 N 天的订单数量）
     * @param days 天数（例如 7 表示近7天）
     * @return 按天的日期标签与对应的订单数量
     */
    public java.util.Map<String, Object> getOrderTrend(int days) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate start = today.minusDays(days - 1);

        // 初始化日期到数量映射
        java.util.LinkedHashMap<String, Long> dateToCount = new java.util.LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            java.time.LocalDate d = start.plusDays(i);
            dateToCount.put(d.toString(), 0L);
        }

        // 统计最近所有订单并按日期累加
        java.util.List<com.example.airplanesalehouduan.admin.entity.AdminOrder> orders = adminOrderRepository.findAll();
        for (com.example.airplanesalehouduan.admin.entity.AdminOrder o : orders) {
            if (o.getCreatedAt() == null) continue;
            java.time.LocalDate d = o.getCreatedAt().toLocalDate();
            if (!d.isBefore(start) && !d.isAfter(today)) {
                String key = d.toString();
                dateToCount.put(key, dateToCount.getOrDefault(key, 0L) + 1);
            }
        }

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("labels", new java.util.ArrayList<>(dateToCount.keySet()));
        result.put("values", new java.util.ArrayList<>(dateToCount.values()));
        return result;
    }

    /**
     * 获取订单类型分布（示例：国内/国际/退票/改签）
     * @return 分布对象，包含每种类型的数量
     */
    public java.util.Map<String, Object> getOrderTypeDistribution() {
        // 使用 tickets.origin_airport 判断国内 / 国际：若 origin_airport 包含英文字符则视为国际，否则视为国内。
        // total 使用 tickets 表的总数作为更贴近实际订单的计数。
        long totalTickets = 0L;
        long international = 0L;
        try {
            Integer t = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tickets", Integer.class);
            totalTickets = (t == null) ? 0L : t.longValue();
            Integer intl = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tickets WHERE origin_airport RLIKE '[A-Za-z]'", Integer.class);
            international = (intl == null) ? 0L : intl.longValue();
        } catch (Exception e) {
            // 回退到 orders 表计数（兼容旧数据结构）
            totalTickets = adminOrderRepository.count();
            international = adminOrderRepository.findAll().stream()
                    .filter(o -> o.getRoute() != null && o.getRoute().matches(".*[A-Za-z].*"))
                    .count();
        }
        long domestic = Math.max(0L, totalTickets - international);

        // 退票与改签数从相应申请表统计
        long refunds = ticketCancelRequestRepository.count();
        long changes = ticketChangeRequestRepository.count();

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("domestic", domestic);
        data.put("international", international);
        data.put("refunds", refunds);
        data.put("changes", changes);
        data.put("total", totalTickets);
        return data;
    }

    /**
     * 获取热门航线（按出现次数排序，返回 route 与 count 的列表）
     */
    public java.util.Map<String, Object> getTopRoutes(int limit) {
        java.util.List<com.example.airplanesalehouduan.admin.entity.AdminOrder> orders = adminOrderRepository.findAll();
        java.util.Map<String, Long> routeCounts = new java.util.HashMap<>();
        for (com.example.airplanesalehouduan.admin.entity.AdminOrder o : orders) {
            if (o.getRoute() == null) continue;
            String route = o.getRoute();
            routeCounts.put(route, routeCounts.getOrDefault(route, 0L) + 1L);
        }

        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        routeCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .forEach(e -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("route", e.getKey());
                    item.put("count", e.getValue());
                    list.add(item);
                });

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("topRoutes", list);
        return result;
    }

    /**
     * 获取运营待处理事项的汇总计数
     */
    public java.util.Map<String, Object> getPendingItems() {
        java.util.Map<String, Object> data = new java.util.HashMap<>();

        // 改签待处理数量
        long flightAdjustments = ticketChangeRequestRepository.countByStatus("待处理");
        // 取消待处理数量
        long cancellationRequests = ticketCancelRequestRepository.countByStatus("待处理");

        // 维护/告警类（以延误或取消的航班数做为示例）
        long maintenanceAlerts = adminFlightRepository.findAll().stream()
                .filter(f -> f.getStatus() == com.example.airplanesalehouduan.admin.entity.AdminFlight.FlightStatus.delayed
                        || f.getStatus() == com.example.airplanesalehouduan.admin.entity.AdminFlight.FlightStatus.cancelled)
                .count();

        data.put("flightAdjustments", flightAdjustments);
        data.put("maintenanceAlerts", maintenanceAlerts);
        data.put("operationalReports", cancellationRequests);
        return data;
    }
}

