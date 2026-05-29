package com.example.vuecourseproject.admin.other;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KuInventoryReportDTO {
    private String id;
    private String name;
    private String category;
    private Integer currentStock;
    private BigDecimal price;
    private BigDecimal totalValue;
}