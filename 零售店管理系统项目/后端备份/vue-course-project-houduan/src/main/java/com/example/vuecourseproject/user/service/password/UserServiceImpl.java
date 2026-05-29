package com.example.vuecourseproject.user.service.password;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.vuecourseproject.user.entity.password.ChangePasswordDTO;
import com.example.vuecourseproject.user.entity.password.User;
import com.example.vuecourseproject.user.exception.password.BusinessException;
import com.example.vuecourseproject.user.mapper.password.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    public void changePassword(String username, ChangePasswordDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!user.getPassword().equals(dto.getOldPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        user.setPassword(dto.getNewPassword());
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
    }

}
