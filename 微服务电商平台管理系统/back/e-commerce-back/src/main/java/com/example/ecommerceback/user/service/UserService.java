package com.example.ecommerceback.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ecommerceback.user.entity.Address;
import com.example.ecommerceback.user.entity.User;
import com.example.ecommerceback.user.mapper.AddressMapper;
import com.example.ecommerceback.user.mapper.UserMapper;
import com.example.ecommerceback.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final long MAX_AVATAR_SIZE = 2L * 1024 * 1024;

    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public Result<User> register(User user) {
        if (user == null || !StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getPassword())) {
            return Result.error(400, "用户名和密码不能为空");
        }

        String username = user.getUsername().trim();
        if (userMapper.selectByUsername(username) != null) {
            return Result.error(400, "用户名已存在");
        }

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPhone(StringUtils.hasText(user.getPhone()) ? user.getPhone().trim() : "");
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (!StringUtils.hasText(user.getRole())) {
            user.setRole("user");
        }

        userMapper.insert(user);
        return Result.success(sanitizeUser(user));
    }

    public Result<User> login(String username, String rawPwd) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPwd)) {
            return Result.error(401, "用户名或密码错误");
        }

        User db = userMapper.selectByUsername(username.trim());
        if (db == null || !matchesPassword(rawPwd, db.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        upgradeLegacyPasswordIfNeeded(db, rawPwd);
        return Result.success(sanitizeUser(db));
    }

    public Result<User> info(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? Result.success(sanitizeUser(user)) : Result.error(404, "用户不存在");
    }

    public Result<List<Address>> getAddressList(Long userId) {
        return Result.success(addressMapper.selectByUserId(userId));
    }

    @Transactional
    public Result<Address> addAddress(Long userId, Address address) {
        if (address == null) {
            return Result.error(400, "地址参数不能为空");
        }

        normalizeAndValidateAddress(address);
        List<Address> existingAddresses = addressMapper.selectByUserId(userId);

        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            addressMapper.updateDefaultByUserId(userId, 0);
        } else if (existingAddresses.isEmpty()) {
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        address.setId(null);
        address.setUserId(userId);
        addressMapper.insert(address);
        return Result.success(addressMapper.selectById(address.getId()));
    }

    @Transactional
    public Result<Address> updateAddress(Long userId, Address address) {
        if (address == null || address.getId() == null) {
            return Result.error(400, "地址ID不能为空");
        }

        Address existing = getOwnedAddress(userId, address.getId());
        if (existing == null) {
            return Result.error(404, "地址不存在");
        }

        normalizeAndValidateAddress(address);

        existing.setReceiver(address.getReceiver());
        existing.setPhone(address.getPhone());
        existing.setProvince(address.getProvince());
        existing.setCity(address.getCity());
        existing.setDistrict(address.getDistrict());
        existing.setDetailAddress(address.getDetailAddress());
        existing.setIsDefault(Integer.valueOf(1).equals(address.getIsDefault()) ? 1 : 0);

        if (Integer.valueOf(1).equals(existing.getIsDefault())) {
            addressMapper.updateDefaultByUserId(userId, 0);
        }

        addressMapper.updateById(existing);
        return Result.success(addressMapper.selectById(existing.getId()));
    }

    @Transactional
    public Result<String> deleteAddress(Long userId, Long addressId) {
        Address existing = getOwnedAddress(userId, addressId);
        if (existing == null) {
            return Result.error(404, "地址不存在");
        }

        addressMapper.deleteById(addressId);

        if (Integer.valueOf(1).equals(existing.getIsDefault())) {
            List<Address> remaining = addressMapper.selectByUserId(userId);
            if (!remaining.isEmpty() && remaining.stream().noneMatch(item -> Integer.valueOf(1).equals(item.getIsDefault()))) {
                Address fallback = remaining.get(0);
                fallback.setIsDefault(1);
                addressMapper.updateById(fallback);
            }
        }

        return Result.success("地址删除成功");
    }

    @Transactional
    public Result<String> setDefaultAddress(Long userId, Long addressId) {
        Address existing = getOwnedAddress(userId, addressId);
        if (existing == null) {
            return Result.error(404, "地址不存在");
        }

        addressMapper.updateDefaultByUserId(userId, 0);
        existing.setIsDefault(1);
        addressMapper.updateById(existing);
        return Result.success("默认地址设置成功");
    }

    @Transactional
    public Result<User> updateUserInfo(Long userId, Map<String, String> updates) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        if (updates.containsKey("username")) {
            String newUsername = trimToNull(updates.get("username"));
            if (!StringUtils.hasText(newUsername)) {
                return Result.error(400, "用户名不能为空");
            }

            User existingUser = userMapper.selectByUsername(newUsername);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                return Result.error(400, "用户名已存在");
            }
            user.setUsername(newUsername);
        }

        if (updates.containsKey("email")) {
            String newEmail = trimToNull(updates.get("email"));
            if (StringUtils.hasText(newEmail) && !isValidEmail(newEmail)) {
                return Result.error(400, "邮箱格式不正确");
            }
            user.setEmail(newEmail);
        }

        if (updates.containsKey("phone")) {
            user.setPhone(trimToNull(updates.get("phone")));
        }

        userMapper.updateById(user);
        return Result.success(sanitizeUser(user));
    }

    @Transactional
    public Result<String> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (!matchesPassword(oldPassword, user.getPassword())) {
            return Result.error(400, "当前密码错误");
        }
        if (oldPassword.equals(newPassword)) {
            return Result.error(400, "新密码不能与当前密码相同");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success("密码修改成功");
    }

    @Transactional
    public Result<User> uploadAvatar(Long userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择头像文件");
        }
        if (!isSupportedImage(file)) {
            return Result.error(400, "仅支持 JPG、PNG、GIF、WEBP 格式图片");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            return Result.error(400, "头像大小不能超过 2MB");
        }

        try {
            Path avatarDir = Paths.get("uploads", "avatars").toAbsolutePath().normalize();
            Files.createDirectories(avatarDir);

            String extension = resolveExtension(file);
            String fileName = "avatar-" + userId + "-" + System.currentTimeMillis() + "." + extension;
            Path target = avatarDir.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String previousAvatar = user.getAvatar();
            user.setAvatar("/api/uploads/avatars/" + fileName);
            userMapper.updateById(user);
            deleteOldAvatar(previousAvatar, avatarDir);

            return Result.success(sanitizeUser(user));
        } catch (IOException exception) {
            return Result.error(500, "头像上传失败: " + exception.getMessage());
        }
    }

    public Result<Page<User>> getUserList(int page, int size, String keyword) {
        try {
            Page<User> pageParam = new Page<>(page, size);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();

            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or()
                    .like("email", keyword)
                    .or()
                    .like("phone", keyword)
                );
            }

            queryWrapper.orderByDesc("id");
            Page<User> result = userMapper.selectPage(pageParam, queryWrapper);
            result.getRecords().forEach(this::sanitizeUser);
            return Result.success(result);
        } catch (Exception exception) {
            return Result.error(500, "获取用户列表失败: " + exception.getMessage());
        }
    }

    @Transactional
    public Result<String> updateUserStatus(Long userId, Integer status) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            user.setStatus(status);
            userMapper.updateById(user);
            return Result.success("用户状态更新成功");
        } catch (Exception exception) {
            return Result.error(500, "更新用户状态失败: " + exception.getMessage());
        }
    }

    @Transactional
    public Result<String> deleteUser(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            QueryWrapper<Address> addressQuery = new QueryWrapper<>();
            addressQuery.eq("user_id", userId);
            addressMapper.delete(addressQuery);
            userMapper.deleteById(userId);
            return Result.success("用户删除成功");
        } catch (Exception exception) {
            return Result.error(500, "删除用户失败: " + exception.getMessage());
        }
    }

    private Address getOwnedAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            return null;
        }
        return address;
    }

    private void normalizeAndValidateAddress(Address address) {
        address.setReceiver(trimToNull(address.getReceiver()));
        address.setPhone(trimToNull(address.getPhone()));
        address.setProvince(trimToNull(address.getProvince()));
        address.setCity(trimToNull(address.getCity()));
        address.setDistrict(trimToNull(address.getDistrict()));
        address.setDetailAddress(trimToNull(address.getDetailAddress()));
        address.setIsDefault(Integer.valueOf(1).equals(address.getIsDefault()) ? 1 : 0);

        if (!StringUtils.hasText(address.getReceiver())
            || !StringUtils.hasText(address.getPhone())
            || !StringUtils.hasText(address.getProvince())
            || !StringUtils.hasText(address.getCity())
            || !StringUtils.hasText(address.getDistrict())
            || !StringUtils.hasText(address.getDetailAddress())) {
            throw new IllegalArgumentException("地址信息不完整");
        }
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }

        if (isBcryptPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        return storedPassword.equals(rawPassword);
    }

    private void upgradeLegacyPasswordIfNeeded(User user, String rawPassword) {
        if (user == null || !StringUtils.hasText(user.getPassword()) || isBcryptPassword(user.getPassword())) {
            return;
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        userMapper.updateById(user);
    }

    private boolean isBcryptPassword(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean isSupportedImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)) {
            return false;
        }

        String normalizedType = contentType.toLowerCase(Locale.ROOT);
        return normalizedType.equals("image/jpeg")
            || normalizedType.equals("image/png")
            || normalizedType.equals("image/gif")
            || normalizedType.equals("image/webp");
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }

        String contentType = file.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return "gif";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }
        return "jpg";
    }

    private void deleteOldAvatar(String avatarUrl, Path avatarDir) throws IOException {
        if (!StringUtils.hasText(avatarUrl) || !avatarUrl.startsWith("/api/uploads/avatars/")) {
            return;
        }

        String fileName = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
        Path oldFile = avatarDir.resolve(fileName).normalize();
        if (oldFile.startsWith(avatarDir)) {
            Files.deleteIfExists(oldFile);
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private User sanitizeUser(User user) {
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
}
