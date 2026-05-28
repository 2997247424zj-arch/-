package com.example.ecommerceback.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("`order`")
public class Order {
    @com.baomidou.mybatisplus.annotation.TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("address_id")
    private Long addressId;

    @TableField("total_amount")
    private Double totalAmount;

    @TableField("pay_amount")
    private Double payAmount;

    private Integer status;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<OrderItem> orderItems;

    @TableField(exist = false)
    private String receiverName;

    @TableField(exist = false)
    private String receiverPhone;

    @TableField(exist = false)
    private String receiverAddress;
}
