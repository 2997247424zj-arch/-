package com.example.vuecourseproject.Login.controller;

import com.example.vuecourseproject.Login.entity.SysUser;
import com.example.vuecourseproject.Login.service.SysUserService;
import com.example.vuecourseproject.user.other.password.Result;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private SysUserService sysUserService;

    // 登录接口 - 使用Result统一返回格式
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody SysUser loginForm) {
        try {
            // 1. 查询用户
            SysUser user = sysUserService.getByUsername(loginForm.getUsername());

            // 2. 校验用户名和密码
            if (user == null || !user.getPassword().equals(loginForm.getPassword())) {
                return (Result<Map<String, Object>>) Result.error("用户名或密码错误");
            }

            // 3. 构造返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", "Bearer " + System.currentTimeMillis());
            data.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole(),
                    "createTime", user.getCreateTime()
            ));

            return Result.success(data);

        } catch (Exception e) {
            e.printStackTrace();
            return (Result<Map<String, Object>>) Result.error("登录失败: " + e.getMessage());
        }
    }

    // 注册接口 - 同样使用Result格式
    @PostMapping("/register")
    public Result<?> register(@RequestBody SysUser registerForm) {
        try {
            // 1. 检查用户名是否已存在
            if (sysUserService.getByUsername(registerForm.getUsername()) != null) {
                return Result.error("用户名已存在");
            }

            // 2. 保存用户
            boolean success = sysUserService.save(registerForm);

            return success ? Result.success("注册成功") : Result.error("注册失败");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    // 全局异常处理 - 也改为Result格式
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return Result.error("参数验证失败");
    }
}