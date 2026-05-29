package com.example.vuecourseproject.admin.controller;

import com.example.vuecourseproject.admin.other.view.*;
import com.example.vuecourseproject.admin.service.OrderVisualizationService;
import com.example.vuecourseproject.user.other.password.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/visualization/orders")
public class OrderVisualizationController {

    private final OrderVisualizationService orderVisualizationService;

    public OrderVisualizationController(OrderVisualizationService orderVisualizationService) {
        this.orderVisualizationService = orderVisualizationService;
    }

    /**
     * 获取销售趋势数据
     */
    @GetMapping("/trend")
    public Result<SalesTrendVO> getSalesTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "day") String granularity) {
        return Result.success(orderVisualizationService.getSalesTrend(startDate, endDate, granularity));
    }

    /**
     * 获取支付方式分布
     */
    @GetMapping("/payment-distribution")
    public Result<List<PaymentDistributionVO>> getPaymentDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(orderVisualizationService.getPaymentDistribution(startDate, endDate));
    }

    /**
     * 获取销售排行数据
     */
    @GetMapping("/sales-ranking")
    public Result<SalesRankingVO> getSalesRanking(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(orderVisualizationService.getSalesRanking(startDate, endDate, limit));
    }

    /**
     * 获取订单来源对比
     */
    @GetMapping("/source-comparison")
    public Result<SourceComparisonVO> getSourceComparison(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(orderVisualizationService.getSourceComparison(startDate, endDate));
    }

    /**
     * 获取订单统计概览
     */
    @GetMapping("/overview")
    public Result<OrderOverviewVO> getOrderOverview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(orderVisualizationService.getOrderOverview(startDate, endDate));
    }
}