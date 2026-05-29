package com.example.vuecourseproject.admin.controller;


import com.example.vuecourseproject.admin.entity.Yonghu;
import com.example.vuecourseproject.admin.other.YonghuException;
import com.example.vuecourseproject.admin.service.YonghuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yonghu")
public class YonghuController {
    private final YonghuService yonghuService;

    @Autowired
    public YonghuController(YonghuService yonghuService) {
        this.yonghuService = yonghuService;
    }

    // 添加用户
    @PostMapping
    public ResponseEntity<?> addYonghu(@RequestBody Yonghu yonghu) {
        try {
            Yonghu savedYonghu = yonghuService.addYonghu(yonghu);
            return ResponseEntity.ok(savedYonghu);
        } catch (YonghuException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 获取所有用户
    @GetMapping
    public ResponseEntity<List<Yonghu>> getAllYonghu() {
        List<Yonghu> yonghuList = yonghuService.getAllYonghu();
        return ResponseEntity.ok(yonghuList);
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public ResponseEntity<?> getYonghuById(@PathVariable Long id) {
        try {
            Yonghu yonghu = yonghuService.getYonghuById(id);
            return ResponseEntity.ok(yonghu);
        } catch (YonghuException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<?> updateYonghu(@PathVariable Long id, @RequestBody Yonghu yonghu) {
        try {
            yonghu.setId(id);
            Yonghu updatedYonghu = yonghuService.updateYonghu(yonghu);
            return ResponseEntity.ok(updatedYonghu);
        } catch (YonghuException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteYonghu(@PathVariable Long id) {
        try {
            yonghuService.deleteYonghu(id);
            return ResponseEntity.ok().build();
        } catch (YonghuException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 根据角色获取用户列表
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getYonghuByRole(@PathVariable String role) {
        try {
            List<Yonghu> yonghuList = yonghuService.getYonghuByRole(role);
            return ResponseEntity.ok(yonghuList);
        } catch (YonghuException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 搜索用户
    @GetMapping("/search")
    public ResponseEntity<List<Yonghu>> searchYonghu(@RequestParam(required = false) String keyword) {
        List<Yonghu> yonghuList = yonghuService.searchYonghu(keyword);
        return ResponseEntity.ok(yonghuList);
    }
    // 仅返回用户总数的端点
    @GetMapping("/count")
    public int getTotalUserCount() {
        return yonghuService.getTotalUserCount();
    }

}