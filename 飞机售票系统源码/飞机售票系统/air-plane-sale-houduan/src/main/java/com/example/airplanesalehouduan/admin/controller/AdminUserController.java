package com.example.airplanesalehouduan.admin.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.Login.entity.User;
import com.example.airplanesalehouduan.admin.dto.AdminUserCreateRequest;
import com.example.airplanesalehouduan.admin.dto.AdminUserUpdateRequest;
import com.example.airplanesalehouduan.admin.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getUserList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        try {
            System.out.println("AdminUserController.getUserList - 接收请求: page=" + page + ", size=" + size + ", keyword=" + keyword + ", role=" + role + ", status=" + status);

            Page<User> userPage = adminUserService.getUserList(page, size, keyword, role, status);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("users", userPage.getContent());
            responseData.put("total", userPage.getTotalElements());
            responseData.put("page", userPage.getNumber());
            responseData.put("size", userPage.getSize());
            responseData.put("totalPages", userPage.getTotalPages());

            System.out.println("AdminUserController.getUserList - 返回数据: total=" + userPage.getTotalElements() + ", users.size()=" + userPage.getContent().size());

            return ResponseEntity.ok(ApiResponse.success("查询成功", responseData));
        } catch (Exception e) {
            System.err.println("AdminUserController.getUserList - 异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getUserById(@PathVariable Integer id) {
        try {
            User user = adminUserService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            return ResponseEntity.ok(ApiResponse.success("查询成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody AdminUserCreateRequest request) {
        try {
            User user = adminUserService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("创建用户成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(
            @PathVariable Integer id,
            @RequestBody AdminUserUpdateRequest request) {
        try {
            User user = adminUserService.updateUser(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新用户成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 切换用户状态（启用/禁用）
     */
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<?>> toggleUserStatus(@PathVariable Integer id) {
        try {
            User user = adminUserService.toggleUserStatus(id);
            return ResponseEntity.ok(ApiResponse.success("状态切换成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Integer id) {
        try {
            adminUserService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("删除用户成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批量导入用户
     */
    @PostMapping("/batch-import")
    public ResponseEntity<ApiResponse<?>> batchImportUsers(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = adminUserService.batchImportUsers(file);
            return ResponseEntity.ok(ApiResponse.success("批量导入完成", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

