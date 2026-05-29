package com.example.vuecourseproject.admin.other;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KuProductDTO {
    private Long id;
    private String name;
    private String barcode;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer isNew;
    private BigDecimal discount;
}