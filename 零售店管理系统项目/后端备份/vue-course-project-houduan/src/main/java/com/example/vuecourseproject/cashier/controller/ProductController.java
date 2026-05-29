package com.example.vuecourseproject.cashier.controller;

import com.example.vuecourseproject.cashier.entity.Product;
import com.example.vuecourseproject.cashier.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String query) {
        if (query == null || query.isEmpty()) {
            return productService.findAll();
        }
        return productService.search(query);
    }
}