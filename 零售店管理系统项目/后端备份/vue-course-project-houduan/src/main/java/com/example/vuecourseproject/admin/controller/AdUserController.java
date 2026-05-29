package com.example.vuecourseproject.admin.controller;

import com.example.vuecourseproject.admin.service.AdUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class AdUserController {
    private final AdUserService userService;

    public AdUserController(AdUserService userService) {
        this.userService = userService;
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return ResponseEntity.ok().body("用户删除成功");
            } else {
                return ResponseEntity.badRequest().body("用户删除失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}