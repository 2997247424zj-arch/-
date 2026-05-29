package com.example.vuecourseproject.admin.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.vuecourseproject.admin.mapper.OrdersMapper;
import com.example.vuecourseproject.admin.mapper.UserOrdersMapper;
import com.example.vuecourseproject.admin.other.view.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderVisualizationServiceImpl implements OrderVisualizationService {

    private final OrdersMapper ordersMapper;
    private final UserOrdersMapper userOrdersMapper;

    @Override
    public SalesTrendVO getSalesTrend(String startDate, String endDate, String granularity) {
        LocalDateTime start = parseDate(startDate, LocalDateTime.now().minusDays(30));
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        // 根据粒度获取数据
        List<SalesTrendDTO> trendData;
        switch (granularity.toLowerCase()) {
            case "hour":
                trendData = ordersMapper.selectHourlySalesTrend(start, end);
                break;
            case "month":
                trendData = ordersMapper.selectMonthlySalesTrend(start, end);
                break;
            case "year":
                trendData = ordersMapper.selectYearlySalesTrend(start, end);
                break;
            case "day":
            default:
                trendData = ordersMapper.selectDailySalesTrend(start, end);
        }

        // 构建返回对象
        SalesTrendVO vo = new SalesTrendVO();
        vo.setLabels(trendData.stream().map(SalesTrendDTO::getTimeLabel).collect(Collectors.toList()));
        vo.setAmounts(trendData.stream().map(SalesTrendDTO::getTotalAmount).collect(Collectors.toList()));
        vo.setCounts(trendData.stream().map(SalesTrendDTO::getOrderCount).collect(Collectors.toList()));
        vo.setGranularity(granularity);

        return vo;
    }

    @Override
    public List<PaymentDistributionVO> getPaymentDistribution(String startDate, String endDate) {
        LocalDateTime start = parseDate(startDate, LocalDateTime.now().minusDays(30));
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        // 获取门店订单支付分布
        List<PaymentDistributionDTO> storePayments = ordersMapper.selectStorePaymentDistribution(start, end);
        // 获取用户订单支付分布
        List<PaymentDistributionDTO> userPayments = userOrdersMapper.selectUserPaymentDistribution(start, end);

        // 合并两种订单来源的支付方式数据
        Map<String, PaymentDistributionVO> paymentMap = new HashMap<>();

        storePayments.forEach(dto -> {
            PaymentDistributionVO vo = paymentMap.computeIfAbsent(dto.getPaymentMethod(),
                    k -> new PaymentDistributionVO(dto.getPaymentMethod(), 0, 0));
            vo.setStoreOrderCount(vo.getStoreOrderCount() + dto.getOrderCount());
            vo.setStoreAmount(vo.getStoreAmount().add(dto.getTotalAmount()));
        });

        userPayments.forEach(dto -> {
            PaymentDistributionVO vo = paymentMap.computeIfAbsent(dto.getPaymentMethod(),
                    k -> new PaymentDistributionVO(dto.getPaymentMethod(), 0, 0));
            vo.setUserOrderCount(vo.getUserOrderCount() + dto.getOrderCount());
            vo.setUserAmount(vo.getUserAmount().add(dto.getTotalAmount()));
        });

        return new ArrayList<>(paymentMap.values());
    }

    @Override
    public SalesRankingVO getSalesRanking(String startDate, String endDate, Integer limit) {
        LocalDateTime start = parseDate(startDate, LocalDateTime.now().minusDays(30));
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        SalesRankingVO vo = new SalesRankingVO();

        // 获取收银员销售排行
        List<SalesRankingDTO> cashierRanking = ordersMapper.selectCashierSalesRanking(start, end, limit);
        vo.setCashierRanking(cashierRanking);

        // 获取用户消费排行
        List<SalesRankingDTO> userRanking = userOrdersMapper.selectUserSalesRanking(start, end, limit);
        vo.setUserRanking(userRanking);

        return vo;
    }

    @Override
    public SourceComparisonVO getSourceComparison(String startDate, String endDate) {
        LocalDateTime start = parseDate(startDate, LocalDateTime.now().minusDays(30));
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        SourceComparisonVO vo = new SourceComparisonVO();

        // 获取门店订单统计
        OrderStatsDTO storeStats = ordersMapper.selectStoreOrderStats(start, end);
        vo.setStoreOrderCount(storeStats.getOrderCount());
        vo.setStoreTotalAmount(storeStats.getTotalAmount());
        vo.setStoreAvgAmount(storeStats.getAvgAmount());

        // 获取用户订单统计
        OrderStatsDTO userStats = userOrdersMapper.selectUserOrderStats(start, end);
        vo.setUserOrderCount(userStats.getOrderCount());
        vo.setUserTotalAmount(userStats.getTotalAmount());
        vo.setUserAvgAmount(userStats.getAvgAmount());

        return vo;
    }

    @Override
    public OrderOverviewVO getOrderOverview(String startDate, String endDate) {
        LocalDateTime start = parseDate(startDate, LocalDateTime.now().minusDays(30));
        LocalDateTime end = parseDate(endDate, LocalDateTime.now());

        OrderOverviewVO vo = new OrderOverviewVO();

        // 总订单数和金额
        OrderStatsDTO totalStats = ordersMapper.selectTotalOrderStats(start, end);
        vo.setTotalOrderCount(totalStats.getOrderCount());
        vo.setTotalAmount(totalStats.getTotalAmount());

        // 今日数据
        OrderStatsDTO todayStats = ordersMapper.selectTodayOrderStats();
        vo.setTodayOrderCount(todayStats.getOrderCount());
        vo.setTodayAmount(todayStats.getTotalAmount());

        // 昨日数据
        OrderStatsDTO yesterdayStats = ordersMapper.selectYesterdayOrderStats();
        vo.setYesterdayOrderCount(yesterdayStats.getOrderCount());
        vo.setYesterdayAmount(yesterdayStats.getTotalAmount());

        // 同比增长率计算
        if (yesterdayStats.getOrderCount() > 0) {
            double growthRate = (todayStats.getOrderCount() - yesterdayStats.getOrderCount()) * 100.0 / yesterdayStats.getOrderCount();
            vo.setGrowthRate(growthRate);
        }

        return vo;
    }

    private LocalDateTime parseDate(String dateStr, LocalDateTime defaultValue) {
        if (StringUtils.isBlank(dateStr)) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }
}