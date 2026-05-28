package com.example.ecommerceback.admin.controller;

import com.example.ecommerceback.admin.service.AdminService;
import com.example.ecommerceback.utils.Result;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpSession session) {
        // 验证管理员权限
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return adminService.getStats();
    }

    /**
     * 获取最近活动
     */
    @GetMapping("/activities")
    public Result<?> getRecentActivities(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }
        
        return adminService.getRecentActivities();
    }
}
