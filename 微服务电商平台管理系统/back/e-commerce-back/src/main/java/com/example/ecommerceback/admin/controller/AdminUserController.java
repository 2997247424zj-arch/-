package com.example.ecommerceback.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ecommerceback.user.entity.User;
import com.example.ecommerceback.user.service.UserService;
import com.example.ecommerceback.utils.Result;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final UserService userService;
    
    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<User>> getUserList(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        HttpSession session
    ) {
        // 验证管理员权限
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return userService.getUserList(page, size, keyword);
    }
    
    /**
     * 启用/禁用用户
     */
    @PutMapping("/status/{userId}")
    public Result<String> updateUserStatus(
        @PathVariable Long userId,
        @RequestParam Integer status,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return userService.updateUserStatus(userId, status);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{userId}")
    public Result<String> deleteUser(
        @PathVariable Long userId,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        Long currentUserId = (Long) session.getAttribute("userId");
        if (userId.equals(currentUserId)) {
            return Result.error(400, "不能删除自己");
        }
        
        return userService.deleteUser(userId);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/detail/{userId}")
    public Result<User> getUserDetail(
        @PathVariable Long userId,
        HttpSession session
    ) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return userService.info(userId);
    }
}
