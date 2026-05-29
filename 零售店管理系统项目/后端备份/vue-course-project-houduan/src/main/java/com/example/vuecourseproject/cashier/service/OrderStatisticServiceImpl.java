package com.example.vuecourseproject.cashier.service;

import com.example.vuecourseproject.cashier.entity.Order;
import com.example.vuecourseproject.cashier.mapper.OrderMapper;
import com.example.vuecourseproject.cashier.service.OrderStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class OrderStatisticServiceImpl implements OrderStatisticService {

    private final OrderMapper orderMapper;

    @Override
    public Map<String, Object> getMonthlyStats(Integer year, String cashierName) {
        List<Order> orders = orderMapper.selectByYearAndCashier(year, cashierName);

        BigDecimal totalSales = orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = orders.stream()
                .map(Order::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalSales", totalSales,
                "totalDiscount", totalDiscount,
                "orderCount", orders.size()
        );
    }

    @Override
    public List<Map<String, Object>> getMonthlyStatsGroupByMonth(
            Integer year,
            String cashierName
    ) {
        // 调用Mapper方法获取按月分组的数据
        List<Map<String, Object>> monthlyData = orderMapper.selectMonthlyStats(year, cashierName);

        // 确保返回12个月的数据，缺失的月份补0
        return IntStream.rangeClosed(1, 12)
                .mapToObj(month -> {
                    String monthKey = String.format("%04d-%02d", year, month);
                    return monthlyData.stream()
                            .filter(m -> m.get("month").equals(monthKey))
                            .findFirst()
                            .orElseGet(() -> createEmptyMonthData(monthKey));
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> createEmptyMonthData(String month) {
        return Map.of(
                "month", month,
                "totalSales", BigDecimal.ZERO,
                "orderCount", 0,
                "totalDiscount", BigDecimal.ZERO
        );
    }
}