package com.example.airplanesalehouduan.Login.service;

import com.example.airplanesalehouduan.Login.dto.LoginRequest;
import com.example.airplanesalehouduan.Login.entity.User;
import com.example.airplanesalehouduan.Login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务类
 * 负责处理用户的注册和登录逻辑
 *
 * 认证方式说明：
 * - 本系统不使用 Token 验证
 * - 改为基于会话的认证方式
 * - 登录成功后，前端将用户信息存储在 sessionStorage 中
 * - 后续 API 请求通过检查 sessionStorage 中的用户信息来验证身份
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户注册
     * @param newUser 包含注册信息的用户对象
     * @return 注册后的用户对象
     * @throws Exception 如果用户名或身份证已存在
     */
    public User register(User newUser) throws Exception {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new Exception("用户名已存在");
        }
        if (newUser.getIdCard() != null && userRepository.existsByIdCard(newUser.getIdCard())) {
            throw new Exception("该身份证号已被注册");
        }
        if (newUser.getPhone() != null && !newUser.getPhone().isEmpty() && userRepository.existsByPhone(newUser.getPhone())) {
            throw new Exception("该手机号已被注册");
        }
        if (newUser.getEmail() != null && !newUser.getEmail().isEmpty() && userRepository.existsByEmail(newUser.getEmail())) {
            throw new Exception("该邮箱已被注册");
        }

        // 警告：密码未加密，直接存储。在生产环境中这是非常危险的。
        // newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        return userRepository.save(newUser);
    }

    /**
     * 用户登录
     * @param loginRequest 包含用户名和密码的请求
     * @return 登录成功的用户对象
     */
    public Optional<User> login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 直接比较明文密码
            if (loginRequest.getPassword().equals(user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}

