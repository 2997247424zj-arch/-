package com.example.vuecourseproject.user.entity.collect;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class CollectFavorite {
    private Long productId;
    private String username;
    private String name;
    private String description;
    private BigDecimal price;
    private Date createTime;
}