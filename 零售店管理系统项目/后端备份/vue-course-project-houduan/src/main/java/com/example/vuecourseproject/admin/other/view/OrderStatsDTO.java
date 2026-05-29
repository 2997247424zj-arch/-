package com.example.vuecourseproject.admin.other.view;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderStatsDTO {
    private Integer orderCount;
    private BigDecimal totalAmount;
    private BigDecimal avgAmount;
}