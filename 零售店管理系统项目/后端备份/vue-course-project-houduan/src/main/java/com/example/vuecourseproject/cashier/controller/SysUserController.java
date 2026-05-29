package com.example.vuecourseproject.cashier.controller;



import com.example.vuecourseproject.Login.entity.SysUser;


import com.example.vuecourseproject.SysUserMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class SysUserController {
    private final SysUserMapper sysUserMapper;
    public SysUserController(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/username/{username}")
    public SysUser getByUsername(@PathVariable String username) {
        return sysUserMapper.selectByUsername(username);
    }
}