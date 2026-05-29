package com.example.vuecourseproject.user.entity.userOrder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("user_orders")
public class UserOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNumber;
    private LocalDateTime createTime;
    private String paymentMethod;
    private String remark;
    private String consignee;
    private String phone;
    private String address;
    private BigDecimal totalAmount;
    private String userName; // 与表结构中的user_name字段对应

    @TableField(exist = false)
    private List<UserOrderItem> items;
}