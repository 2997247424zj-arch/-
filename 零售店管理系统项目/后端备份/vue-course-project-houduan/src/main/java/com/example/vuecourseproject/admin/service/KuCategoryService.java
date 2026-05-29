package com.example.vuecourseproject.admin.service;

import com.example.vuecourseproject.admin.other.KuCategoryDTO;

import java.util.List;

public interface KuCategoryService {
    List<KuCategoryDTO> getEnabledCategories();
}