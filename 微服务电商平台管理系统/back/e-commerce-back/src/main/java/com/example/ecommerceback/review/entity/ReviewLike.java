package com.example.ecommerceback.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review_like")
public class ReviewLike {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("review_id")
    private Long reviewId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("create_time")
    private LocalDateTime createTime;
}
