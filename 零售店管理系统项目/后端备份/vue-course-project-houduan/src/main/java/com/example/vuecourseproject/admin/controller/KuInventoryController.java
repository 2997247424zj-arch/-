package com.example.vuecourseproject.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vuecourseproject.admin.other.KuInventoryReportDTO;
import com.example.vuecourseproject.admin.other.KuProductDTO;
import com.example.vuecourseproject.admin.service.KuProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ku/inventory")
public class KuInventoryController {

    private final KuProductService productService;

    @Autowired
    public KuInventoryController(KuProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public Page<KuProductDTO> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return productService.getProductPage(page, size, keyword);
    }

    @GetMapping("/product/{id}")
    public KuProductDTO getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/report")
    public List<KuInventoryReportDTO> getInventoryReport(@RequestParam(required = false) String keyword) {
        return productService.getInventoryReport(keyword);
    }

    @GetMapping("/total-value")
    public BigDecimal getTotalInventoryValue() {
        return productService.getTotalInventoryValue();
    }

    @PostMapping("/restock")
    public ResponseEntity<?> restockProduct(
            @RequestBody Map<String, Object> requestBody) {  // 改为使用请求体接收
        try {
            Long productId = Long.valueOf(requestBody.get("productId").toString());
            Integer amount = Integer.valueOf(requestBody.get("amount").toString());
            productService.restockProduct(productId, amount);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage(), "status", "error"));
        }
    }
    @GetMapping("/distribution")
    public Map<String, BigDecimal> getInventoryDistribution() {
        return productService.getInventoryDistribution();
    }

}