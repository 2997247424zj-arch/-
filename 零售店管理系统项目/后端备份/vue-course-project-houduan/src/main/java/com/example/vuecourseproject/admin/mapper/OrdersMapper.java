package com.example.vuecourseproject.admin.mapper;

import com.example.vuecourseproject.admin.other.view.OrderStatsDTO;
import com.example.vuecourseproject.admin.other.view.PaymentDistributionDTO;
import com.example.vuecourseproject.admin.other.view.SalesRankingDTO;
import com.example.vuecourseproject.admin.other.view.SalesTrendDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper {

    // 按小时获取销售趋势
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d %H:00') AS time_label, " +
            "COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY time_label " +
            "ORDER BY time_label")
    List<SalesTrendDTO> selectHourlySalesTrend(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    // 按天获取销售趋势
    @Select("SELECT DATE(create_time) AS time_label, " +
            "COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY time_label " +
            "ORDER BY time_label")
    List<SalesTrendDTO> selectDailySalesTrend(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    // 按月获取销售趋势
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS time_label, " +
            "COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY time_label " +
            "ORDER BY time_label")
    List<SalesTrendDTO> selectMonthlySalesTrend(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    // 按年获取销售趋势
    @Select("SELECT YEAR(create_time) AS time_label, " +
            "COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY time_label " +
            "ORDER BY time_label")
    List<SalesTrendDTO> selectYearlySalesTrend(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    // 支付方式分布
    @Select("SELECT payment_method, COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY payment_method")
    List<PaymentDistributionDTO> selectStorePaymentDistribution(@Param("start") LocalDateTime start,
                                                                @Param("end") LocalDateTime end);

    // 收银员销售排行
    @Select("SELECT cashier_name AS name, COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY cashier_name " +
            "ORDER BY total_amount DESC " +
            "LIMIT #{limit}")
    List<SalesRankingDTO> selectCashierSalesRanking(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end,
                                                    @Param("limit") Integer limit);

    // 门店订单统计
    @Select("SELECT COUNT(*) AS order_count, SUM(total) AS total_amount, AVG(total) AS avg_amount " +
            "FROM orders " +
            "WHERE create_time BETWEEN #{start} AND #{end}")
    OrderStatsDTO selectStoreOrderStats(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // 总订单统计
    @Select("SELECT COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE create_time BETWEEN #{start} AND #{end}")
    OrderStatsDTO selectTotalOrderStats(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // 今日订单统计
    @Select("SELECT COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE DATE(create_time) = CURRENT_DATE")
    OrderStatsDTO selectTodayOrderStats();

    // 昨日订单统计
    @Select("SELECT COUNT(*) AS order_count, SUM(total) AS total_amount " +
            "FROM orders " +
            "WHERE DATE(create_time) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)")
    OrderStatsDTO selectYesterdayOrderStats();
}

