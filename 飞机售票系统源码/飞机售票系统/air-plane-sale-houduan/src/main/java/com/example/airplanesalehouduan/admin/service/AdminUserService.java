package com.example.airplanesalehouduan.admin.service;

import com.example.airplanesalehouduan.Login.entity.User;
import com.example.airplanesalehouduan.Login.repository.UserRepository;
import com.example.airplanesalehouduan.admin.dto.AdminUserCreateRequest;
import com.example.airplanesalehouduan.admin.dto.AdminUserUpdateRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 管理员用户管理服务
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 分页查询用户列表
     * 使用 EntityManager 和 CriteriaBuilder 实现动态查询
     */
    public Page<User> getUserList(Integer page, Integer size, String keyword, String role, String status) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registrationTime"));

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> query = cb.createQuery(User.class);
            Root<User> root = query.from(User.class);

            List<Predicate> predicates = new ArrayList<>();

            // 关键词搜索（姓名或用户名）
            if (keyword != null && !keyword.trim().isEmpty()) {
                Predicate namePredicate = cb.like(cb.lower(root.get("realName")), "%" + keyword.toLowerCase() + "%");
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), "%" + keyword.toLowerCase() + "%");
                predicates.add(cb.or(namePredicate, usernamePredicate));
            }

            // 角色筛选
            if (role != null && !role.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("role"), role));
            }

            // 状态筛选
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            query.where(predicates.toArray(new Predicate[0]));
            query.orderBy(cb.desc(root.get("registrationTime")));

            TypedQuery<User> typedQuery = entityManager.createQuery(query);

            // 获取总数
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<User> countRoot = countQuery.from(User.class);
            countQuery.select(cb.count(countRoot));
            if (!predicates.isEmpty()) {
                // 重新构建相同的谓词用于计数查询
                List<Predicate> countPredicates = new ArrayList<>();
                if (keyword != null && !keyword.trim().isEmpty()) {
                    Predicate namePredicate = cb.like(cb.lower(countRoot.get("realName")), "%" + keyword.toLowerCase() + "%");
                    Predicate usernamePredicate = cb.like(cb.lower(countRoot.get("username")), "%" + keyword.toLowerCase() + "%");
                    countPredicates.add(cb.or(namePredicate, usernamePredicate));
                }
                if (role != null && !role.trim().isEmpty()) {
                    countPredicates.add(cb.equal(countRoot.get("role"), role));
                }
                if (status != null && !status.trim().isEmpty()) {
                    countPredicates.add(cb.equal(countRoot.get("status"), status));
                }
                countQuery.where(countPredicates.toArray(new Predicate[0]));
            }
            Long total = entityManager.createQuery(countQuery).getSingleResult();

            // 分页
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
            List<User> users = typedQuery.getResultList();

            System.out.println("AdminUserService.getUserList - 查询结果: total=" + total + ", users.size()=" + users.size());

            return new PageImpl<>(users, pageable, total);
        } catch (Exception e) {
            System.err.println("AdminUserService.getUserList - 查询异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(AdminUserCreateRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 注意：实际应该加密存储
        user.setRealName(request.getName());
        user.setIdCard(request.getIdCard());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : "passenger");
        user.setStatus("active");

        return userRepository.save(user);
    }

    /**
     * 更新用户
     */
    @Transactional
    public User updateUser(Integer id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 如果修改用户名，检查是否冲突
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getName() != null) {
            user.setRealName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(request.getPassword()); // 注意：实际应该加密存储
        }

        return userRepository.save(user);
    }

    /**
     * 切换用户状态
     */
    @Transactional
    public User toggleUserStatus(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if ("active".equals(user.getStatus())) {
            user.setStatus("disabled");
        } else {
            user.setStatus("active");
        }

        return userRepository.save(user);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 批量导入用户
     * 支持CSV格式文件，格式：姓名,用户名,身份证号,手机,角色,password
     */
    @Transactional
    public Map<String, Object> batchImportUsers(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        int successCount = 0;
        int totalCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                totalCount++;

                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // 跳过空行
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] parts = line.split(",");
                    if (parts.length < 6) {
                        errors.add(Map.of(
                                "row", totalCount,
                                "message", "数据格式不正确，期望6列：姓名,用户名,身份证号,手机,角色,password"
                        ));
                        continue;
                    }

                    String name = parts[0].trim();
                    String username = parts[1].trim();
                    String idCard = parts[2].trim();
                    String phone = parts[3].trim();
                    String role = parts[4].trim();
                    String password = parts[5].trim();

                    // 验证必填字段
                    if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                        errors.add(Map.of(
                                "row", totalCount,
                                "message", "姓名、用户名和密码为必填项"
                        ));
                        continue;
                    }

                    // 检查用户名是否已存在
                    if (userRepository.existsByUsername(username)) {
                        errors.add(Map.of(
                                "row", totalCount,
                                "message", "用户名已存在: " + username
                        ));
                        continue;
                    }

                    // 检查身份证号是否已存在（如果提供了）
                    if (!idCard.isEmpty() && userRepository.existsByIdCard(idCard)) {
                        errors.add(Map.of(
                                "row", totalCount,
                                "message", "身份证号已存在: " + idCard
                        ));
                        continue;
                    }

                    // 创建用户
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(password); // 注意：实际应该加密存储
                    user.setRealName(name);
                    user.setIdCard(idCard.isEmpty() ? null : idCard);
                    user.setPhone(phone.isEmpty() ? null : phone);
                    user.setRole(role.isEmpty() ? "passenger" : role);
                    user.setStatus("active");

                    userRepository.save(user);
                    successCount++;

                } catch (Exception e) {
                    errors.add(Map.of(
                            "row", totalCount,
                            "message", "处理失败: " + e.getMessage()
                    ));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("文件读取失败: " + e.getMessage());
        }

        result.put("total", totalCount - 1); // 减去标题行
        result.put("succeeded", successCount);
        result.put("failed", errors.size());
        result.put("errors", errors);

        return result;
    }
}

