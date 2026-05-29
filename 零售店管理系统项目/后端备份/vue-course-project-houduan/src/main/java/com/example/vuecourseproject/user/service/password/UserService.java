package com.example.vuecourseproject.user.service.password;

import com.example.vuecourseproject.user.entity.password.ChangePasswordDTO;

public interface UserService {
    void changePassword(String username, ChangePasswordDTO dto);
}
