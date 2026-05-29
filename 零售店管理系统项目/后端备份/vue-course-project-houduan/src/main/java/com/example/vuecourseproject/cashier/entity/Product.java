package com.example.vuecourseproject.cashier.entity;



import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String barcode;
    private Double price;
    private String image;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
