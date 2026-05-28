package com.example.ecommerceback.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ecommerceback.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    @Select("SELECT * FROM product WHERE category_id = #{categoryId} AND status = 1 ORDER BY id DESC")
    List<Product> selectByCategory(Long categoryId);
}
