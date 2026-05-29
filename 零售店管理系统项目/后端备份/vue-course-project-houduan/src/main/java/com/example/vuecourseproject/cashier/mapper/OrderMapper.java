package com.example.vuecourseproject.cashier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuecourseproject.cashier.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE " +
            "YEAR(create_time) = #{year} AND " +
            "(cashier_name = #{cashierName} OR #{cashierName} = '') " +
            "ORDER BY create_time")
    List<Order> selectByYearAndCashier(
            @Param("year") Integer year,
            @Param("cashierName") String cashierName
    );

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, " +
            "SUM(total) AS totalSales, " +
            "COUNT(*) AS orderCount, " +
            "SUM(discount) AS totalDiscount " +
            "FROM orders WHERE " +
            "YEAR(create_time) = #{year} AND " +
            "(cashier_name = #{cashierName} OR #{cashierName} = '') " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> selectMonthlyStats(
            @Param("year") Integer year,
            @Param("cashierName") String cashierName
    );
}