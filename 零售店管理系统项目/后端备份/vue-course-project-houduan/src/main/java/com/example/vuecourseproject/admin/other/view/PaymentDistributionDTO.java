package com.example.vuecourseproject.admin.other.view;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDistributionDTO {
    private String paymentMethod;
    private Integer orderCount;
    private BigDecimal totalAmount;
}