package com.example.vuecourseproject.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("product")
public class KuProductEntity {
    private Long id;
    private String name;
    private String barcode;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String image;
    private String description;
    private Long categoryId;
    private Integer isNew;
    private BigDecimal discount;
    private Integer stock;
    private Date createTime;
    private Date updateTime;
}