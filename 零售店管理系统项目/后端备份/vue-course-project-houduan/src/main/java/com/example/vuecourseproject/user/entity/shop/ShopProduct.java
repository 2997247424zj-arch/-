package com.example.vuecourseproject.user.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class ShopProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String barcode;
    private BigDecimal price;
    @TableField("original_price")
    private BigDecimal originalPrice;
    private String image;
    private String description;
    @TableField("category_id")
    private Long categoryId;
    @TableField("is_new")
    private Boolean isNew;
    private BigDecimal discount;
    private String tags; // 实际存储为JSON字符串
    private String specs; // 实际存储为JSON字符串
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;

    // 关联的分类信息
    @TableField(exist = false)
    private ShopProductCategory category;
}

