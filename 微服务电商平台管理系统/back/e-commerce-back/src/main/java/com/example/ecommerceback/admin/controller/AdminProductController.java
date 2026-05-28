package com.example.ecommerceback.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.service.ProductService;
import com.example.ecommerceback.utils.Result;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final ProductService productService;
    
    /**
     * 获取商品列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<Product>> getProductList(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long categoryId,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return productService.getProductList(page, size, keyword, categoryId);
    }
    
    /**
     * 添加商品
     */
    @PostMapping("/add")
    public Result<Product> addProduct(
        @RequestBody Product product,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return productService.addProduct(product);
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/update")
    public Result<Product> updateProduct(
        @RequestBody Product product,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return productService.updateProduct(product);
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteProduct(
        @PathVariable Long id,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return productService.deleteProduct(id);
    }
    
    /**
     * 商品上下架
     */
    @PutMapping("/status/{id}")
    public Result<String> updateProductStatus(
        @PathVariable Long id,
        @RequestParam Integer status,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return productService.updateProductStatus(id, status);
    }
    
    /**
     * 更新商品库存
     */
    @PutMapping("/stock/{id}")
    public Result<String> updateProductStock(
        @PathVariable Long id,
        @RequestParam Integer stock,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return productService.updateProductStock(id, stock);
    }
}
