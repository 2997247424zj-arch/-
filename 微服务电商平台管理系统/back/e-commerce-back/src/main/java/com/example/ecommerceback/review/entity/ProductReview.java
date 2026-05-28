package com.example.ecommerceback.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_review")
public class ProductReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("product_id")
    private Long productId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("order_id")
    private Long orderId;
    
    private Integer rating; // 评分 1-5星
    
    private String content; // 评价内容
    
    private String images; // 评价图片(多张用逗号分隔)
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String username; // 用户名
    
    @TableField(exist = false)
    private String userAvatar; // 用户头像
    
    @TableField(exist = false)
    private Integer likeCount; // 点赞数
    
    @TableField(exist = false)
    private Boolean isLiked; // 当前用户是否点赞
}
