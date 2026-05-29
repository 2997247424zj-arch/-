package com.example.vuecourseproject;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuecourseproject.Login.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(String username);
}