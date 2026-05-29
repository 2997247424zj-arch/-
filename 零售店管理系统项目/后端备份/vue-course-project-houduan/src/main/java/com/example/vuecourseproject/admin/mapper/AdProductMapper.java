package com.example.vuecourseproject.admin.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface AdProductMapper {
    /**
     * 根据ID删除商品
     */
    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 检查商品是否存在
     */
    @Select("SELECT COUNT(*) FROM product WHERE id = #{id}")
    boolean existsById(@Param("id") Long id);

    /**
     * 获取商品分类ID（用于删除后检查分类）
     */
    @Select("SELECT category_id FROM product WHERE id = #{id}")
    Long getCategoryId(@Param("id") Long id);
}