package com.example.airplanesalehouduan.Login.repository;

import com.example.airplanesalehouduan.Login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 检查身份证号是否存在
    boolean existsByIdCard(String idCard);

    // 检查手机号是否存在
    boolean existsByPhone(String phone);

    // 检查邮箱是否存在
    boolean existsByEmail(String email);
}

