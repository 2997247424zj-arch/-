package com.example.vuecourseproject.user.other.cart;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long productId;
    private String username;
    private Integer quantity;
}