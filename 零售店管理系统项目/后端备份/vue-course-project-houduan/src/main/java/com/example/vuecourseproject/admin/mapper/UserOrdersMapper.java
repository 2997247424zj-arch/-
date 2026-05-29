package com.example.vuecourseproject.admin.mapper;

import com.example.vuecourseproject.admin.other.view.OrderStatsDTO;
import com.example.vuecourseproject.admin.other.view.PaymentDistributionDTO;
import com.example.vuecourseproject.admin.other.view.SalesRankingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserOrdersMapper {

    // 用户订单支付方式分布
    @Select("SELECT payment_method, COUNT(*) AS order_count, SUM(total_amount) AS total_amount " +
            "FROM user_orders " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY payment_method")
    List<PaymentDistributionDTO> selectUserPaymentDistribution(@Param("start") LocalDateTime start,
                                                               @Param("end") LocalDateTime end);

    // 用户消费排行
    @Select("SELECT user_name AS name, COUNT(*) AS order_count, SUM(total_amount) AS total_amount " +
            "FROM user_orders " +
            "WHERE create_time BETWEEN #{start} AND #{end} " +
            "GROUP BY user_name " +
            "ORDER BY total_amount DESC " +
            "LIMIT #{limit}")
    List<SalesRankingDTO> selectUserSalesRanking(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("limit") Integer limit);

    // 用户订单统计
    @Select("SELECT COUNT(*) AS order_count, SUM(total_amount) AS total_amount, AVG(total_amount) AS avg_amount " +
            "FROM user_orders " +
            "WHERE create_time BETWEEN #{start} AND #{end}")
    OrderStatsDTO selectUserOrderStats(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}