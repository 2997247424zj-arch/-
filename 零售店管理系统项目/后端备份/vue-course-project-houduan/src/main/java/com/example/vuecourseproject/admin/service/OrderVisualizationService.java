package com.example.vuecourseproject.admin.service;


import com.example.vuecourseproject.admin.other.view.*;

import java.util.List;

/**
 * 订单数据可视化服务接口
 */
public interface OrderVisualizationService {

    /**
     * 获取销售趋势数据
     */
    SalesTrendVO getSalesTrend(String startDate, String endDate, String granularity);

    /**
     * 获取支付方式分布
     */
    List<PaymentDistributionVO> getPaymentDistribution(String startDate, String endDate);

    /**
     * 获取销售排行数据
     */
    SalesRankingVO getSalesRanking(String startDate, String endDate, Integer limit);

    /**
     * 获取订单来源对比
     */
    SourceComparisonVO getSourceComparison(String startDate, String endDate);

    /**
     * 获取订单统计概览
     */
    OrderOverviewVO getOrderOverview(String startDate, String endDate);
}