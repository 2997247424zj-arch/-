package com.example.vuecourseproject.user.controller.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.vuecourseproject.user.entity.shop.ShopProduct;
import com.example.vuecourseproject.user.other.password.Result;
import com.example.vuecourseproject.user.service.shop.ShopProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/shop/products")
public class ShopProductController {

    @Autowired
    private ShopProductService productService;

    @GetMapping
    public Result<Page<ShopProduct>> getProducts(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "sortBy", defaultValue = "default") String sortBy,
            @RequestParam(value = "page", defaultValue = "1") int pageNum,
            @RequestParam(value = "size", defaultValue = "12") int pageSize) {

        Page<ShopProduct> page = productService.getProductsWithFilter(
                categoryId, keyword, minPrice, maxPrice, sortBy, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<ShopProduct> getProductDetail(@PathVariable Long id) {
        ShopProduct product = productService.getById(id);
        if (product == null) {
            return (Result<ShopProduct>) Result.error("商品不存在");
        }
        return Result.success(product);
    }

    @GetMapping("/new")
    public Result<List<ShopProduct>> getNewProducts() {
        List<ShopProduct> products = productService.getNewProducts();
        return Result.success(products);
    }

    @GetMapping("/discounted")
    public Result<List<ShopProduct>> getDiscountedProducts() {
        List<ShopProduct> products = productService.getDiscountedProducts();
        return Result.success(products);
    }
}