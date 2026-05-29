package com.example.vuecourseproject.admin.service;

import com.example.vuecourseproject.admin.mapper.AdUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class AdUserService {
    private final AdUserMapper userMapper;

    // 使用构造器注入
    public AdUserService(AdUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否删除成功
     * @throws RuntimeException 当用户不存在时抛出异常
     */
    @Transactional
    public boolean deleteUser(Long id) {
        // 1. 检查用户是否存在
        if (!userExists(id)) {
            throw new RuntimeException("用户不存在，ID: " + id);
        }

        // 2. 执行删除操作
        int affectedRows = userMapper.deleteUserById(id);

        // 3. 返回操作结果
        return affectedRows > 0;
    }

    /**
     * 检查用户是否存在
     * @param id 用户ID
     * @return 存在返回true，不存在返回false
     */
    public boolean userExists(Long id) {
        return userMapper.checkUserExists(id) > 0;
    }
}