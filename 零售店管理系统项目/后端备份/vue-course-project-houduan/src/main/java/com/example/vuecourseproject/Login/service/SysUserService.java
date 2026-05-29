package com.example.vuecourseproject.Login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuecourseproject.Login.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser getByUsername(String username);
}