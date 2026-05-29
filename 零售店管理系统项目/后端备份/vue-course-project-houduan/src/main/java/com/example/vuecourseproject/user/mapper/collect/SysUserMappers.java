package com.example.vuecourseproject.user.mapper.collect;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuecourseproject.Login.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMappers extends BaseMapper<SysUser> {

    @Select("SELECT EXISTS(SELECT 1 FROM sys_user WHERE username = #{username})")
    boolean existsByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    SysUser selectByUsername(String username);
}