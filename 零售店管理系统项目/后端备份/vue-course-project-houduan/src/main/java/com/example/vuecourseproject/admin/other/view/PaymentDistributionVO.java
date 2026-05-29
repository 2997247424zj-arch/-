package com.example.vuecourseproject.admin.other.view;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDistributionVO {
    private String paymentMethod;
    private Integer storeOrderCount;
    private BigDecimal storeAmount;
    private Integer userOrderCount;
    private BigDecimal userAmount;

    public PaymentDistributionVO(String paymentMethod, Integer storeOrderCount, Integer userOrderCount) {
        this.paymentMethod = paymentMethod;
        this.storeOrderCount = storeOrderCount;
        this.userOrderCount = userOrderCount;
        this.storeAmount = BigDecimal.ZERO;
        this.userAmount = BigDecimal.ZERO;
    }
}