package com.example.vuecourseproject.admin.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface AdProductCategoryMapper {
    /**
     * 检查分类是否还有关联商品
     */
    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    int countProductsByCategory(@Param("categoryId") Long categoryId);
}