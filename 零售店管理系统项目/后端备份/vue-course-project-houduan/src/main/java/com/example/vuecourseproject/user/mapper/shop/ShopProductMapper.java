// ShopProductMapper.java
package com.example.vuecourseproject.user.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuecourseproject.user.entity.shop.ShopProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Mapper
@Repository
public interface ShopProductMapper extends BaseMapper<ShopProduct> {
    @Select("SELECT * FROM product WHERE category_id = #{categoryId} AND status = 1")
    List<ShopProduct> selectByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT * FROM product WHERE name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%')")
    List<ShopProduct> searchProducts(@Param("keyword") String keyword);

    @Select("SELECT * FROM product WHERE price BETWEEN #{minPrice} AND #{maxPrice}")
    List<ShopProduct> selectByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
}