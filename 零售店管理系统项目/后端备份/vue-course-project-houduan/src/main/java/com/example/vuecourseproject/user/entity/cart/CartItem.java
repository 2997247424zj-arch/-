package com.example.vuecourseproject.user.entity.cart;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.vuecourseproject.user.entity.shop.ShopProduct;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("cart_item")
public class CartItem {
    @TableId(type = IdType.AUTO)  // 修改为AUTO类型
    private Long id;
    private String username;
    private String name;
    private BigDecimal price;
    @TableField("original_price")
    private BigDecimal originalPrice;
    private String description;
    private BigDecimal discount;
    private Integer quantity;

    // 关联的商品信息（非表字段）
    @TableField(exist = false)
    private ShopProduct product;
}