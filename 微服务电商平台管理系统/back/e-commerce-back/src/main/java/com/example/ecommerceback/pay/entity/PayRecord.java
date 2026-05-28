package com.example.ecommerceback.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pay_record")
public class PayRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("pay_amount")
    private Double payAmount;

    @TableField("pay_type")
    private Integer payType;

    @TableField("pay_status")
    private Integer status;

    @TableField("pay_no")
    private String payNo;

    @TableField("pay_time")
    private LocalDateTime finishTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
