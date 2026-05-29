package com.example.vuecourseproject.cashier.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.vuecourseproject.cashier.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
