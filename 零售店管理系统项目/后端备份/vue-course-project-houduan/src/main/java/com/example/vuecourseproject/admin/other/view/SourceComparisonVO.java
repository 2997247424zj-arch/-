package com.example.vuecourseproject.admin.other.view;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SourceComparisonVO {
    private Integer storeOrderCount;
    private BigDecimal storeTotalAmount;
    private BigDecimal storeAvgAmount;
    private Integer userOrderCount;
    private BigDecimal userTotalAmount;
    private BigDecimal userAvgAmount;
}