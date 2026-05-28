package com.example.ecommerceback.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ecommerceback.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("SELECT * FROM `order` WHERE user_id = #{userId}")
    List<Order> selectByUser(Long userId);
}
