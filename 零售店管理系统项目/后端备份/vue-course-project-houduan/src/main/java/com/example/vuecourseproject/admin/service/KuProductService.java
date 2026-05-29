package com.example.vuecourseproject.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.vuecourseproject.admin.other.KuInventoryReportDTO;
import com.example.vuecourseproject.admin.other.KuProductDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface KuProductService {
    Page<KuProductDTO> getProductPage(int page, int size, String keyword);

    KuProductDTO getProductById(Long id);

    List<KuInventoryReportDTO> getInventoryReport(String keyword);

    BigDecimal getTotalInventoryValue();

    void restockProduct(Long productId, Integer amount);
    Map<String, BigDecimal> getInventoryDistribution();
}