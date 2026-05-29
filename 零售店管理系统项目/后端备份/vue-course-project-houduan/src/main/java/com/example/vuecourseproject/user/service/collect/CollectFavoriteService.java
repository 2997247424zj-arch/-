package com.example.vuecourseproject.user.service.collect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.vuecourseproject.Login.entity.SysUser;
import com.example.vuecourseproject.user.entity.collect.CollectFavorite;
import com.example.vuecourseproject.user.entity.shop.ShopProduct;
import com.example.vuecourseproject.user.mapper.collect.CollectFavoriteMapper;

import com.example.vuecourseproject.user.mapper.collect.SysUserMappers;
import com.example.vuecourseproject.user.mapper.shop.ShopProductMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CollectFavoriteService {

    @Autowired
    private CollectFavoriteMapper favoriteMapper;

    @Autowired
    private ShopProductMapper productMapper;

    @Autowired
    private SysUserMappers userMapper;

    @Transactional
    public void addFavorite(Long productId, String username) {
        // 1. 验证用户是否存在（使用MyBatis-Plus的exists方法）
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(SysUser::getUsername, username);
        if (!userMapper.exists(userQuery)) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 检查是否已收藏
        if (favoriteMapper.existsByProductIdAndUsername(productId, username)) {
            throw new RuntimeException("该商品已收藏");
        }

        // 3. 验证商品是否存在
        ShopProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 4. 创建收藏记录
        CollectFavorite favorite = new CollectFavorite();
        favorite.setProductId(productId);
        favorite.setUsername(username);
        favorite.setName(product.getName());
        favorite.setDescription(product.getDescription());
        favorite.setPrice(product.getPrice());

        // 5. 保存到数据库
        favoriteMapper.insert(favorite);
    }

    @Transactional
    public void removeFavorite(Long productId, String username) {
        // 1. 验证用户存在性（可选，根据业务需求）
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(SysUser::getUsername, username);
        if (!userMapper.exists(userQuery)) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 执行删除
        int affectedRows = favoriteMapper.deleteByProductIdAndUsername(productId, username);
        if (affectedRows == 0) {
            throw new RuntimeException("收藏记录不存在或已被删除");
        }
    }

    public List<CollectFavorite> getFavorites(String username, String keyword, int page, int size) {
        // 参数校验
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;

        // 计算偏移量
        int offset = (page - 1) * size;

        // 根据是否有关键词选择查询方式
        if (keyword == null || keyword.trim().isEmpty()) {
            return favoriteMapper.selectAllByUsername(username, offset, size);
        }
        return favoriteMapper.selectByKeywordAndUsername(keyword, username, offset, size);
    }

    @Transactional
    public void clearFavorites(String username) {
        // 1. 验证用户存在性
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(SysUser::getUsername, username);
        if (!userMapper.exists(userQuery)) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 执行清空
        int deletedCount = favoriteMapper.deleteAllByUsername(username);
        // 可以记录日志：logger.info("用户 {} 清空了 {} 条收藏记录", username, deletedCount);
    }
}