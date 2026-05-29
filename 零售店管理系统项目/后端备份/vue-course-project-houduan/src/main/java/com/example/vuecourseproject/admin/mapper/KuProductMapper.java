package com.example.vuecourseproject.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuecourseproject.admin.entity.KuProductEntity;
import com.example.vuecourseproject.admin.other.KuProductWithStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KuProductMapper extends BaseMapper<KuProductEntity> {
    @Select("SELECT p.id as productId, p.name as productName, p.category_id as categoryId, " +
            "p.price as price, p.stock as stock " +
            "FROM product p " +
            "WHERE (#{keyword} IS NULL OR p.name LIKE CONCAT('%', #{keyword}, '%') OR p.barcode LIKE CONCAT('%', #{keyword}, '%'))")
    List<KuProductWithStock> selectProductsWithStock(@Param("keyword") String keyword);

    @Update("UPDATE product SET stock = stock + #{amount} WHERE id = #{productId}")
    int restockProduct(@Param("productId") Long productId, @Param("amount") Integer amount);
}