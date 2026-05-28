package com.example.ecommerceback.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ecommerceback.cart.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<CartItem> {
    @Select("SELECT * FROM cart WHERE user_id = #{userId} ORDER BY id DESC")
    List<CartItem> selectByUser(Long userId);
}
