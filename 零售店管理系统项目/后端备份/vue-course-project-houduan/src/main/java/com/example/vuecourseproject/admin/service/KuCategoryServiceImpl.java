package com.example.vuecourseproject.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.vuecourseproject.admin.entity.KuProductCategoryEntity;
import com.example.vuecourseproject.admin.mapper.KuProductCategoryMapper;
import com.example.vuecourseproject.admin.other.KuCategoryDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KuCategoryServiceImpl implements KuCategoryService {

    private final KuProductCategoryMapper categoryMapper;

    public KuCategoryServiceImpl(KuProductCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<KuCategoryDTO> getEnabledCategories() {
        List<KuProductCategoryEntity> entities = categoryMapper.selectList(
                new QueryWrapper<KuProductCategoryEntity>()
                        .eq("status", 1)
                        .orderByAsc("sort")
        );

        return entities.stream().map(entity -> {
            KuCategoryDTO dto = new KuCategoryDTO();
            BeanUtils.copyProperties(entity, dto);
            return dto;
        }).collect(Collectors.toList());
    }
}