package com.example.vuecourseproject.admin.service;


import com.example.vuecourseproject.admin.entity.Yonghu;
import com.example.vuecourseproject.admin.mapper.YonghuMapper;
import com.example.vuecourseproject.admin.other.YonghuException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class YonghuService {
    private final YonghuMapper yonghuMapper;

    @Autowired
    public YonghuService(YonghuMapper yonghuMapper) {
        this.yonghuMapper = yonghuMapper;
    }

    // 添加用户
    public Yonghu addYonghu(Yonghu yonghu) {
        // 验证角色是否合法
        if (!isValidRole(yonghu.getRole())) {
            throw new YonghuException("角色只能是admin、cashier或user");
        }

        // 检查用户名是否已存在
        if (yonghuMapper.selectYonghuByUsername(yonghu.getUsername()) != null) {
            throw new YonghuException("用户名已存在");
        }

        yonghuMapper.insertYonghu(yonghu);
        return yonghu;
    }

    // 获取所有用户
    public List<Yonghu> getAllYonghu() {
        return yonghuMapper.selectAllYonghu();
    }

    // 根据ID获取用户
    public Yonghu getYonghuById(Long id) {
        return yonghuMapper.selectYonghuById(id);
    }

    // 更新用户信息
    public Yonghu updateYonghu(Yonghu yonghu) {
        // 验证角色是否合法
        if (!isValidRole(yonghu.getRole())) {
            throw new YonghuException("角色只能是admin、cashier或user");
        }

        Yonghu existingYonghu = yonghuMapper.selectYonghuById(yonghu.getId());
        if (existingYonghu == null) {
            throw new YonghuException("用户不存在");
        }

        // 检查用户名是否被其他用户使用
        Yonghu sameUsername = yonghuMapper.selectYonghuByUsername(yonghu.getUsername());
        if (sameUsername != null && !sameUsername.getId().equals(yonghu.getId())) {
            throw new YonghuException("用户名已被其他用户使用");
        }

        yonghuMapper.updateYonghu(yonghu);
        return yonghu;
    }

    // 删除用户
    public void deleteYonghu(Long id) {
        if (yonghuMapper.selectYonghuById(id) == null) {
            throw new YonghuException("用户不存在");
        }
        yonghuMapper.deleteYonghu(id);
    }

    // 根据角色获取用户列表
    public List<Yonghu> getYonghuByRole(String role) {
        if (!isValidRole(role)) {
            throw new YonghuException("角色只能是admin、cashier或user");
        }
        return yonghuMapper.selectYonghuByRole(role);
    }

    // 搜索用户
    public List<Yonghu> searchYonghu(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return yonghuMapper.selectAllYonghu();
        }
        return yonghuMapper.searchYonghu(keyword);
    }

    // 验证角色是否合法
    private boolean isValidRole(String role) {
        return "admin".equals(role) || "cashier".equals(role) || "user".equals(role);
    }

    // 仅返回用户总数
    public int getTotalUserCount() {
        return yonghuMapper.countAllUsers();
    }

}