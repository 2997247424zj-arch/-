package com.example.airplanesalehouduan.Login.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.example.airplanesalehouduan.Login.dto.LoginRequest;
import com.example.airplanesalehouduan.Login.dto.RegisterRequest;
import com.example.airplanesalehouduan.Login.dto.EmailRegisterRequest;
import com.example.airplanesalehouduan.Login.entity.User;
import com.example.airplanesalehouduan.Login.service.UserService;
import com.example.airplanesalehouduan.sms.SmsService;
import com.example.airplanesalehouduan.email.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.example.airplanesalehouduan.Login.dto.PhoneRegisterRequest;
import java.util.UUID;

/**
 * 用户认证控制器
 * 注意：本系统不使用 Token 验证，改为基于会话的认证方式
 * 用户登录成功后，前端将用户信息存储在 sessionStorage 中
 * 所有 API 请求不需要 Authorization 头
 */
@RestController
@RequestMapping("/api/auth") // 所有接口的基础路径
public class UserController {

    private final UserService userService;

    private final SmsService smsService;

    private final EmailService emailService;

    // 使用构造器注入，避免字段注入的反模式警告
    public UserController(UserService userService, SmsService smsService, EmailService emailService) {
        this.userService = userService;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    /**
     * 注册接口
     * @param registerRequest 从请求体中获取的用户信息（携带短信验证码）
     * @return 统一的API响应
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            // 如果前端传了手机号＋验证码，则先校验验证码
            if (registerRequest.getPhone() != null && registerRequest.getSmsCode() != null) {
                boolean ok = smsService.checkVerifyCode(registerRequest.getPhone(), registerRequest.getSmsCode());
                if (!ok) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("短信验证码校验失败"));
                }
            }

            // 构造 User 实体并注册
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setIdCard(registerRequest.getIdCard());
            user.setPhone(registerRequest.getPhone());
            user.setRealName(registerRequest.getRealName());
            if (registerRequest.getRole() != null) user.setRole(registerRequest.getRole());

            User registeredUser = userService.register(user);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", registeredUser.getId());
            responseData.put("username", registeredUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("注册成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 手机号快捷注册：手机号 + 密码 + 短信验证码
     * 请求体示例：{ "phone": "13800138000", "smsCode": "123456", "password": "userpassword" }
     */
    @PostMapping("/register/phone")
    public ResponseEntity<ApiResponse<?>> registerByPhone(@RequestBody PhoneRegisterRequest req) {
        try {
            String phone = req.getPhone();
            String code = req.getSmsCode();
            String password = req.getPassword();
            if (phone == null || phone.isEmpty() || code == null || code.isEmpty() || password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("手机号、验证码和密码不能为空"));
            }
            boolean ok = smsService.checkVerifyCode(phone, code);
            if (!ok) {
                return ResponseEntity.badRequest().body(ApiResponse.error("短信验证码校验失败"));
            }

            // 检查手机号是否已被注册
            if (userService != null) {
                // userService.register 检查手机号唯一性，但需要先构造 User
            }

            // 使用手机号作为用户名，使用前端传递的密码
            User user = new User();
            user.setUsername(phone); // 直接使用手机号作为用户名
            user.setPassword(password); // 使用前端传递的密码
            user.setPhone(phone);
            user.setRole("passenger");
            user.setRealName(null);
            user.setIdCard(null);

            User registeredUser = userService.register(user);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", registeredUser.getId());
            responseData.put("username", registeredUser.getUsername());
            responseData.put("phone", registeredUser.getPhone());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("注册成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 发送邮箱验证码
     * 请求体示例：{ "email": "user@example.com" }
     */
    @PostMapping("/send-email-code")
    public ResponseEntity<ApiResponse<?>> sendEmailCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("邮箱不能为空"));
            }

            boolean success = emailService.sendVerifyCode(email);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("验证码已发送到邮箱", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("发送验证码失败，请稍后重试"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("发送验证码失败：" + e.getMessage()));
        }
    }

    /**
     * 邮箱注册：邮箱 + 验证码 + 其他信息
     * 请求体示例：{ "email": "user@example.com", "emailCode": "123456", "username": "testuser", "password": "123456", ... }
     */
    @PostMapping("/register/email")
    public ResponseEntity<ApiResponse<?>> registerByEmail(@RequestBody EmailRegisterRequest req) {
        try {
            String email = req.getEmail();
            String code = req.getEmailCode();
            String username = req.getUsername();
            String password = req.getPassword();

            if (email == null || email.isEmpty() || code == null || code.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("邮箱和验证码不能为空"));
            }

            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("用户名和密码不能为空"));
            }

            // 校验邮箱验证码
            boolean codeValid = emailService.checkVerifyCode(email, code);
            if (!codeValid) {
                return ResponseEntity.badRequest().body(ApiResponse.error("邮箱验证码校验失败"));
            }

            // 构造 User 实体并注册
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setRealName(req.getRealName());
            user.setIdCard(req.getIdCard());
            user.setPhone(req.getPhone());
            user.setRole("passenger");

            User registeredUser = userService.register(user);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("userId", registeredUser.getId());
            responseData.put("username", registeredUser.getUsername());
            responseData.put("email", registeredUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("注册成功", responseData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 登录接口
     * @param loginRequest 从请求体中获取的登录信息
     * @return 统一的API响应
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> loginUser(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userService.login(loginRequest);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!"active".equals(user.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("账户状态异常，请联系管理员"));
            }
            // 登录成功，返回部分用户信息
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("id", user.getId());
            userProfile.put("username", user.getUsername());
            userProfile.put("realName", user.getRealName());
            userProfile.put("phone", user.getPhone());
            userProfile.put("role", user.getRole());
            userProfile.put("registeredAt", user.getRegisterTime()); // LocalDateTime

            return ResponseEntity.ok(ApiResponse.success("登录成功", userProfile));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("用户名或密码错误"));
        }
    }
}

