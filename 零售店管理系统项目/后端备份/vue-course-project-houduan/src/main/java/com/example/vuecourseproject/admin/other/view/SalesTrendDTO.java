package com.example.vuecourseproject.admin.other.view;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesTrendDTO {
    private String timeLabel;
    private Integer orderCount;
    private BigDecimal totalAmount;
}