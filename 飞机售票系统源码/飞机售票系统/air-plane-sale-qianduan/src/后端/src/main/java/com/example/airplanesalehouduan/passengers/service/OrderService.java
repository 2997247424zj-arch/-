package com.example.airplanesalehouduan.passengers.service;




import com.example.airplanesalehouduan.passengers.entity.Order;
import com.example.airplanesalehouduan.passengers.other.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务类
 * 负责处理订单相关的业务逻辑
 * 实现乘客数据隔离，确保用户只能查询自己的订单
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * 获取乘客的订单列表（分页）
     * @param passengerId 乘客ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 订单分页结果
     */
    public Page<Order> getOrdersByPassengerId(Integer passengerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId, pageable);
    }

    /**
     * 获取乘客的订单列表（支持多条件筛选）
     * @param passengerId 乘客ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param orderNumber 订单号（可选，模糊查询）
     * @param customer 客户姓名（可选，模糊查询）
     * @param status 订单状态（可选）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @return 订单分页结果
     */
    public Page<Order> getOrdersByPassengerIdWithFilters(
            Integer passengerId,
            int page,
            int size,
            String orderNumber,
            String customer,
            String status,
            String startDate,
            String endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 必须匹配乘客ID
            predicates.add(cb.equal(root.get("passengerId"), passengerId));

            // 订单号搜索
            if (orderNumber != null && !orderNumber.trim().isEmpty()) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderNumber + "%"));
            }

            // 客户姓名搜索
            if (customer != null && !customer.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("passengerName")), "%" + customer.toLowerCase() + "%"));
            }

            // 状态筛选
            if (status != null && !status.trim().isEmpty()) {
                String s = status.trim();
                // 扩展：支持中文/英文混合的“待出行”概念，后端应匹配多种可能的状态值
                // 当前端传入 '待出行' 或 'ticketed' 或 '已出票' 时，我们将匹配包含“出行”的中文状态、'已出票' 及英文 'ticketed'
                if ("待出行".equals(s) || "已出票".equals(s) || "ticketed".equalsIgnoreCase(s)) {
                    Predicate pEqualPending = cb.equal(root.get("status"), "待出行");
                    Predicate pEqualTicketed = cb.equal(root.get("status"), "已出票");
                    Predicate pEqualTicketedEn = cb.equal(cb.lower(root.get("status")), "ticketed");
                    Predicate pLikeUpcoming = cb.like(root.get("status"), "%出行%");
                    predicates.add(cb.or(pEqualPending, pEqualTicketed, pEqualTicketedEn, pLikeUpcoming));
                } else {
                    predicates.add(cb.equal(root.get("status"), status));
                }
            }

            // 日期范围筛选（使用createdAt字段）
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable);
    }

    /**
     * 根据状态获取乘客的订单列表（分页）
     * @param passengerId 乘客ID
     * @param status 订单状态（字符串类型）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 订单分页结果
     */
    public Page<Order> getOrdersByPassengerIdAndStatus(
            Integer passengerId,
            String status,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByPassengerIdAndStatusOrderByCreatedAtDesc(passengerId, status, pageable);
    }

    /**
     * 获取乘客最近的订单（用于快捷接口）
     * @param passengerId 乘客ID
     * @param limit 限制数量
     * @return 订单列表
     */
    public List<Order> getRecentOrdersByPassengerId(Integer passengerId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findRecentOrdersByPassengerId(passengerId, pageable);
    }

    /**
     * 根据订单号获取订单详情（验证订单是否属于该乘客）
     * @param passengerId 乘客ID
     * @param orderNo 订单号
     * @return 订单对象
     * @throws Exception 如果订单不存在或不属于该乘客
     */
    public Order getOrderByOrderNo(Integer passengerId, String orderNo) throws Exception {
        Optional<Order> orderOptional = orderRepository.findByPassengerIdAndOrderNo(passengerId, orderNo);
        if (orderOptional.isPresent()) {
            return orderOptional.get();
        }
        throw new Exception("订单不存在或无权限访问");
    }

    /**
     * 根据订单ID获取订单详情（验证订单是否属于该乘客）
     * @param passengerId 乘客ID
     * @param orderId 订单ID
     * @return 订单对象
     * @throws Exception 如果订单不存在或不属于该乘客
     */
    public Order getOrderById(Integer passengerId, Long orderId) throws Exception {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            if (!order.getPassengerId().equals(passengerId)) {
                throw new Exception("无权限访问该订单");
            }
            return order;
        }
        throw new Exception("订单不存在");
    }

    /**
     * 获取乘客的订单统计信息
     * @param passengerId 乘客ID
     * @return 统计信息Map
     */
    public Map<String, Object> getOrderStatistics(Integer passengerId) {
        Map<String, Object> statistics = new HashMap<>();

        // 总订单数
        long totalOrders = orderRepository.countByPassengerId(passengerId);
        statistics.put("totalOrders", totalOrders);

        // 从数据库动态获取所有状态，并统计各状态订单数量
        List<String> statusList = orderRepository.findDistinctStatusByPassengerId(passengerId);
        for (String status : statusList) {
            long count = orderRepository.countByPassengerIdAndStatus(passengerId, status);
            statistics.put(status, count);
        }

        return statistics;
    }

    /**
     * 获取完整的统计数据（包括总消费、平均消费、订单增长等）
     * @param passengerId 乘客ID
     * @return 完整统计数据
     */
    public Map<String, Object> getCompleteStatistics(Integer passengerId) {
        Map<String, Object> stats = new HashMap<>();

        // 总订单数
        long totalOrders = orderRepository.countByPassengerId(passengerId);
        stats.put("totalOrders", totalOrders);

        // 总消费金额
        BigDecimal totalSpent = orderRepository.sumTotalAmountByPassengerId(passengerId);
        if (totalSpent == null) {
            totalSpent = BigDecimal.ZERO;
        }
        stats.put("totalSpent", totalSpent.doubleValue());

        // 平均订单金额
        BigDecimal averageSpent = totalOrders > 0
                ? totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        stats.put("averageSpent", averageSpent.doubleValue());

        // 订单增长百分比（对比上个月）
        double orderGrowth = calculateOrderGrowth(passengerId);
        stats.put("orderGrowth", orderGrowth);

        // 从数据库动态获取状态并统计
        // 不再硬编码状态值，直接从数据库查询所有订单并统计
        Pageable allPageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<Order> allOrders = orderRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId, allPageable).getContent();
        long upcomingFlights = 0;
        long completedFlights = 0;

        // 根据实际数据库中的状态值进行统计
        // 遍历所有订单，根据状态值进行统计（支持中文状态）
        for (Order order : allOrders) {
            String status = order.getStatus();
            if (status != null) {
                // 根据状态值判断（支持中文状态）
                if (status.contains("出行") || status.contains("待出行") || status.contains("已出票")) {
                    upcomingFlights++;
                }
                if (status.contains("完成") || status.contains("已完成") || status.contains("已创建")) {
                    completedFlights++;
                }
            }
        }

        stats.put("upcomingFlights", upcomingFlights);
        stats.put("completedFlights", completedFlights);

        return stats;
    }

    /**
     * 计算订单增长百分比（对比上个月）
     * @param passengerId 乘客ID
     * @return 增长百分比
     */
    private double calculateOrderGrowth(Integer passengerId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thisMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = thisMonthStart.minusSeconds(1);

        long thisMonthCount = orderRepository.countByPassengerIdAndDateRange(
                passengerId, thisMonthStart, now);
        long lastMonthCount = orderRepository.countByPassengerIdAndDateRange(
                passengerId, lastMonthStart, lastMonthEnd);

        if (lastMonthCount == 0) {
            return thisMonthCount > 0 ? 100.0 : 0.0;
        }

        double growth = ((double)(thisMonthCount - lastMonthCount) / lastMonthCount) * 100;
        return Math.round(growth * 10.0) / 10.0; // 保留一位小数
    }

    /**
     * 获取消费趋势数据（近6个月）
     * @param passengerId 乘客ID
     * @return 消费趋势列表
     */
    public List<Map<String, Object>> getSpendingTrend(Integer passengerId) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 获取近6个月的数据
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            BigDecimal monthSpending = orderRepository.sumTotalAmountByPassengerIdAndDateRange(
                    passengerId, monthStart, monthEnd);
            if (monthSpending == null) {
                monthSpending = BigDecimal.ZERO;
            }

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", formatMonth(monthStart));
            monthData.put("value", monthSpending.intValue());

            trend.add(monthData);
        }

        return trend;
    }

    /**
     * 获取月度对比数据（订单数 vs 消费额）
     * @param passengerId 乘客ID
     * @return 月度对比列表
     */
    public List<Map<String, Object>> getMonthlyComparison(Integer passengerId) {
        List<Map<String, Object>> comparison = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 获取近6个月的数据
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

            long orders = orderRepository.countByPassengerIdAndDateRange(
                    passengerId, monthStart, monthEnd);
            BigDecimal spending = orderRepository.sumTotalAmountByPassengerIdAndDateRange(
                    passengerId, monthStart, monthEnd);
            if (spending == null) {
                spending = BigDecimal.ZERO;
            }

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", formatMonth(monthStart));
            monthData.put("orders", orders);
            monthData.put("spending", spending.intValue());

            comparison.add(monthData);
        }

        return comparison;
    }

    /**
     * 获取订单状态分布
     * @param passengerId 乘客ID
     * @return 状态分布列表
     */
    public List<Map<String, Object>> getOrderStatusDistribution(Integer passengerId) {
        List<Map<String, Object>> distribution = new ArrayList<>();

        long total = orderRepository.countByPassengerId(passengerId);

        // 从数据库动态获取所有状态值
        List<String> statusList = orderRepository.findDistinctStatusByPassengerId(passengerId);

        // 为每个状态分配颜色（可以根据状态值动态分配）
        String[] colors = {"#10b981", "#6b7280", "#3b82f6", "#ef4444", "#f59e0b", "#8b5cf6", "#ec4899"};
        int colorIndex = 0;

        for (String status : statusList) {
            String color = colors[colorIndex % colors.length];
            colorIndex++;

            long count = orderRepository.countByPassengerIdAndStatus(passengerId, status);
            int percentage = total > 0 ? (int) Math.round((count * 100.0) / total) : 0;

            Map<String, Object> statusInfo = createStatusInfo(status, color);
            statusInfo.put("count", count);
            statusInfo.put("percentage", percentage);

            distribution.add(statusInfo);
        }

        // 过滤掉数量为0的状态
        return distribution.stream()
                .filter(item -> (Long) item.get("count") > 0)
                .collect(Collectors.toList());
    }

    /**
     * 创建状态信息
     * 直接使用状态字符串作为 label，不进行中英文转换
     */
    private Map<String, Object> createStatusInfo(String status, String color) {
        Map<String, Object> info = new HashMap<>();
        info.put("label", status);  // 直接使用状态字符串
        info.put("color", color);
        return info;
    }

    /**
     * 获取热门航线
     * @param passengerId 乘客ID
     * @param limit 限制数量
     * @return 热门航线列表
     */
    public List<Map<String, Object>> getPopularRoutes(Integer passengerId, int limit) {
        List<Order> orders = orderRepository.findAllByPassengerIdWithRoute(passengerId);

        // 统计航线出现次数
        Map<String, Long> routeCounts = orders.stream()
                .filter(o -> o.getRoute() != null && !o.getRoute().trim().isEmpty())
                .collect(Collectors.groupingBy(Order::getRoute, Collectors.counting()));

        // 按次数排序并取前N个
        List<Map<String, Object>> popularRoutes = routeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> routeInfo = new HashMap<>();
                    routeInfo.put("route", entry.getKey());
                    routeInfo.put("count", entry.getValue().intValue());
                    routeInfo.put("color", getRouteColor(entry.getValue().intValue()));
                    return routeInfo;
                })
                .collect(Collectors.toList());

        return popularRoutes;
    }

    /**
     * 根据次数获取航线颜色
     */
    private String getRouteColor(int count) {
        String[] colors = {"#1E8AE6", "#0A2F63", "#ec4899", "#f59e0b", "#10b981"};
        return colors[Math.min(count - 1, colors.length - 1)];
    }

    /**
     * 格式化月份显示（如：6月、7月）
     */
    private String formatMonth(LocalDateTime dateTime) {
        int month = dateTime.getMonthValue();
        return month + "月";
    }

    /**
     * 删除订单（验证订单是否属于该乘客）
     * @param passengerId 乘客ID
     * @param orderId 订单ID
     * @throws Exception 如果订单不存在或不属于该乘客
     */
    public void deleteOrder(Integer passengerId, Long orderId) throws Exception {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            // 验证订单是否属于该乘客
            if (!order.getPassengerId().equals(passengerId)) {
                throw new Exception("无权限删除该订单");
            }
            // 删除订单
            orderRepository.deleteById(orderId);
        } else {
            throw new Exception("订单不存在");
        }
    }
}



