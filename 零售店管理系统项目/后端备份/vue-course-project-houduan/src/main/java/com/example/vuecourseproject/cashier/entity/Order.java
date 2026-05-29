package com.example.vuecourseproject.cashier.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("cashier_name")
    private String cashierName;

    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 关键修改：指定日期格式
    private LocalDateTime createTime;
}