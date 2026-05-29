package com.example.vuecourseproject.user.other.echart;



import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserExpenseStatsDTO {
    private String timePeriod; // 月份或年份，如 "2023-01" 或 "2023"
    private BigDecimal totalAmount; // 总支出金额
}