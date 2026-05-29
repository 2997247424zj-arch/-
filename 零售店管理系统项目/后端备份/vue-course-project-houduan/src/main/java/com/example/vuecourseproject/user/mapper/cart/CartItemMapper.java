package com.example.vuecourseproject.user.mapper.cart;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuecourseproject.user.entity.cart.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
    // 基础CRUD方法已由BaseMapper提供
}