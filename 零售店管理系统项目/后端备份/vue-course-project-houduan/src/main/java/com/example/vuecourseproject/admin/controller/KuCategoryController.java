package com.example.vuecourseproject.admin.controller;

import com.example.vuecourseproject.admin.other.KuCategoryDTO;
import com.example.vuecourseproject.admin.service.KuCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ku/category")
public class KuCategoryController {

    private final KuCategoryService categoryService;

    @Autowired
    public KuCategoryController(KuCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/enabled")
    public List<KuCategoryDTO> getEnabledCategories() {
        return categoryService.getEnabledCategories();
    }
}