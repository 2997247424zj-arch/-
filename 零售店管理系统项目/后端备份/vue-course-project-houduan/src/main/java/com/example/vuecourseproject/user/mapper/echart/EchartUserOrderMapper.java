package com.example.vuecourseproject.user.mapper.echart;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.vuecourseproject.user.entity.userOrder.UserOrder;
import com.example.vuecourseproject.user.other.echart.UserExpenseStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EchartUserOrderMapper extends BaseMapper<UserOrder> {

    // 按月统计用户支出
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS timePeriod, " +
            "SUM(total_amount) AS totalAmount " +
            "FROM user_orders " +
            "WHERE user_name = #{username} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') " +
            "ORDER BY timePeriod")
    List<UserExpenseStatsDTO> getMonthlyExpenseStats(@Param("username") String username);

    // 按年统计用户支出
    @Select("SELECT DATE_FORMAT(create_time, '%Y') AS timePeriod, " +
            "SUM(total_amount) AS totalAmount " +
            "FROM user_orders " +
            "WHERE user_name = #{username} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y') " +
            "ORDER BY timePeriod")
    List<UserExpenseStatsDTO> getYearlyExpenseStats(@Param("username") String username);
}
