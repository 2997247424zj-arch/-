// ShopProductCategoryMapper.java
package com.example.vuecourseproject.user.mapper.shop;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuecourseproject.user.entity.shop.ShopProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ShopProductCategoryMapper extends BaseMapper<ShopProductCategory> {
    @Select("SELECT * FROM product_category WHERE status = 1 ORDER BY sort ASC")
    List<ShopProductCategory> selectAllEnabled();
}