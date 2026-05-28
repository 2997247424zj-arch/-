package com.example.ecommerceback.user.controller;

import com.example.ecommerceback.user.entity.Address;
import com.example.ecommerceback.user.entity.User;
import com.example.ecommerceback.user.service.UserService;
import com.example.ecommerceback.utils.Result;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> body, HttpSession session) {
        Result<User> result = userService.login(body.get("username"), body.get("password"));
        if (result.getCode() == 200 && result.getData() != null) {
            User user = result.getData();
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
        }
        return result;
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        session.invalidate();
        return Result.success("退出成功");
    }

    @GetMapping("/info")
    public Result<User> info(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.info(userId);
    }

    @PutMapping("/update")
    public Result<User> updateUserInfo(HttpSession session, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.updateUserInfo(userId, body);
    }

    @PostMapping("/change-password")
    public Result<String> changePassword(HttpSession session, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || oldPassword.isBlank()) {
            return Result.error(400, "请输入当前密码");
        }
        if (newPassword == null || newPassword.isBlank()) {
            return Result.error(400, "请输入新密码");
        }
        if (newPassword.length() < 6) {
            return Result.error(400, "新密码长度不能少于 6 位");
        }

        return userService.changePassword(userId, oldPassword, newPassword);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<User> uploadAvatar(HttpSession session, @RequestPart("file") MultipartFile file) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.uploadAvatar(userId, file);
    }

    @GetMapping("/address/list")
    public Result<List<Address>> getAddressList(HttpSession session) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.getAddressList(userId);
    }

    @PostMapping("/address/add")
    public Result<Address> addAddress(HttpSession session, @RequestBody Address address) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            return userService.addAddress(userId, address);
        } catch (IllegalArgumentException exception) {
            return Result.error(400, exception.getMessage());
        }
    }

    @PutMapping("/address/update")
    public Result<Address> updateAddress(HttpSession session, @RequestBody Address address) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            return userService.updateAddress(userId, address);
        } catch (IllegalArgumentException exception) {
            return Result.error(400, exception.getMessage());
        }
    }

    @DeleteMapping("/address/delete/{id}")
    public Result<String> deleteAddress(HttpSession session, @PathVariable Long id) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.deleteAddress(userId, id);
    }

    @PutMapping("/address/default/{id}")
    public Result<String> setDefaultAddress(HttpSession session, @PathVariable Long id) {
        Long userId = getCurrentUserId(session);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return userService.setDefaultAddress(userId, id);
    }

    private Long getCurrentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }
}
