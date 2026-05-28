package com.example.ecommerceback.product.controller;

import com.example.ecommerceback.product.entity.Product;
import com.example.ecommerceback.product.entity.ProductCategory;
import com.example.ecommerceback.product.service.ProductService;
import com.example.ecommerceback.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/category/list")
    public Result<List<ProductCategory>> listCategories() {
        return productService.listCategories();
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> listAll(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) String sortBy
    ) {
        return productService.queryProducts(page, size, categoryId, keyword, minPrice, maxPrice, sortBy);
    }

    @GetMapping("/search")
    public Result<Map<String, Object>> search(
        @RequestParam String keyword,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) String sortBy
    ) {
        return productService.queryProducts(page, size, categoryId, keyword, minPrice, maxPrice, sortBy);
    }

    @GetMapping("/category/{id}")
    public Result<List<Product>> listByCategory(@PathVariable Long id) {
        return productService.listByCategory(id);
    }

    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return productService.detail(id);
    }

    @GetMapping("/stock/{id}")
    public Result<Integer> stock(@PathVariable Long id) {
        return productService.stock(id);
    }

    @PostMapping("/create")
    public Result<Product> create(@RequestBody Product product) {
        return productService.create(product);
    }
}
