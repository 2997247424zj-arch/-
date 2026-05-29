package com.example.vuecourseproject.admin.other.view;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderOverviewVO {
    private Integer totalOrderCount;
    private BigDecimal totalAmount;
    private Integer todayOrderCount;
    private BigDecimal todayAmount;
    private Integer yesterdayOrderCount;
    private BigDecimal yesterdayAmount;
    private Double growthRate;
}