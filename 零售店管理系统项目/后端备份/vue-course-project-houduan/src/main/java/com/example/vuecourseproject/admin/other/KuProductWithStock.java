package com.example.vuecourseproject.admin.other;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KuProductWithStock {
    private Long productId;
    private String productName;
    private Long categoryId;
    private BigDecimal price;
    private Integer stock;
}