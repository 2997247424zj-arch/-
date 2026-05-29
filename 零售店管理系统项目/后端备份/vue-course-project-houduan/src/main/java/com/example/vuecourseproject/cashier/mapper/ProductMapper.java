package com.example.vuecourseproject.cashier.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.example.vuecourseproject.cashier.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
