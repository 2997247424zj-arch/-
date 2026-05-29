package com.example.vuecourseproject.user.service.shop;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.vuecourseproject.user.entity.shop.ShopProductCategory;
import com.example.vuecourseproject.user.mapper.shop.ShopProductCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class ShopProductCategoryServiceImpl extends ServiceImpl<ShopProductCategoryMapper, ShopProductCategory>
        implements ShopProductCategoryService {

    @Override
    public List<ShopProductCategory> getAllCategories() {
        return baseMapper.selectAllEnabled();
    }

    @Override
    public ShopProductCategory getCategoryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean addCategory(ShopProductCategory category) {
        return baseMapper.insert(category) > 0;
    }

    @Override
    public boolean updateCategory(ShopProductCategory category) {
        return baseMapper.updateById(category) > 0;
    }

    @Override
    public boolean deleteCategory(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public boolean saveBatch(Collection<ShopProductCategory> entityList) {
        return super.saveBatch(entityList);
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<ShopProductCategory> entityList) {
        return super.saveOrUpdateBatch(entityList);
    }
}
