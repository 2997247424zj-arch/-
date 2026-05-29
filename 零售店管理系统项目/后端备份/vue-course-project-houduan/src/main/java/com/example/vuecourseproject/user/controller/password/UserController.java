package com.example.vuecourseproject.user.controller.password;

import com.example.vuecourseproject.user.entity.password.ChangePasswordDTO;


import com.example.vuecourseproject.user.other.password.Result;
import com.example.vuecourseproject.user.service.password.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/change-password")
    public Result<?> changePassword(
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody ChangePasswordDTO dto
    ) {
        userService.changePassword(username, dto);
        return Result.success("密码修改成功");
    }
}