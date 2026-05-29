package com.example.vuecourseproject.user.controller.shop;


import com.example.vuecourseproject.user.entity.shop.ShopProductCategory;
import com.example.vuecourseproject.user.other.password.Result;
import com.example.vuecourseproject.user.service.shop.ShopProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/categories")
public class ShopProductCategoryController {

    @Autowired
    private ShopProductCategoryService categoryService;

    @GetMapping
    public Result<List<ShopProductCategory>> getAllCategories() {
        List<ShopProductCategory> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @GetMapping("/{id}")
    public Result<ShopProductCategory> getCategoryById(@PathVariable Long id) {
        ShopProductCategory category = categoryService.getCategoryById(id);
        if (category == null) {
            return (Result<ShopProductCategory>) Result.error("分类不存在");
        }
        return Result.success(category);
    }

    @PostMapping
    public Result<Boolean> addCategory(@RequestBody ShopProductCategory category) {
        boolean success = categoryService.addCategory(category);
        return success ? Result.success(true) : (Result<Boolean>) Result.error("添加分类失败");
    }

    @PutMapping("/{id}")
    public Result<Boolean> updateCategory(@PathVariable Long id, @RequestBody ShopProductCategory category) {
        category.setId(id);
        boolean success = categoryService.updateCategory(category);
        return success ? Result.success(true) : (Result<Boolean>) Result.error("更新分类失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        boolean success = categoryService.deleteCategory(id);
        return success ? Result.success(true) : (Result<Boolean>) Result.error("删除分类失败");
    }
}
