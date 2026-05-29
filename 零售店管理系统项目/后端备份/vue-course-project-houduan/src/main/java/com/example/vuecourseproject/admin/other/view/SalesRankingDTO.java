package com.example.vuecourseproject.admin.other.view;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesRankingDTO {
    private String name;
    private Integer orderCount;
    private BigDecimal totalAmount;
}