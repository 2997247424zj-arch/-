package com.example.vuecourseproject.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("product_category")
public class KuProductCategoryEntity {
    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
}