package com.example.vuecourseproject.user.service.shop;



import com.example.vuecourseproject.user.entity.shop.ShopProductCategory;

import java.util.List;

public interface ShopProductCategoryService {
    List<ShopProductCategory> getAllCategories();

    ShopProductCategory getCategoryById(Long id);

    boolean addCategory(ShopProductCategory category);

    boolean updateCategory(ShopProductCategory category);

    boolean deleteCategory(Long id);
}

