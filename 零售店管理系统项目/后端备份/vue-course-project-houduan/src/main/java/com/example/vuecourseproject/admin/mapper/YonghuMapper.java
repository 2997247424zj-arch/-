package com.example.vuecourseproject.admin.mapper;


import com.example.vuecourseproject.admin.entity.Yonghu;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface YonghuMapper {
    // 添加用户
    @Insert("INSERT INTO sys_user(username, password, role) VALUES(#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertYonghu(Yonghu yonghu);

    // 根据ID查询用户
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    Yonghu selectYonghuById(Long id);

    // 查询所有用户
    @Select("SELECT * FROM sys_user")
    List<Yonghu> selectAllYonghu();

    // 根据用户名查询用户
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    Yonghu selectYonghuByUsername(String username);

    // 更新用户信息
    @Update("UPDATE sys_user SET username=#{username}, password=#{password}, role=#{role} WHERE id=#{id}")
    int updateYonghu(Yonghu yonghu);

    // 删除用户
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteYonghu(Long id);

    // 根据角色查询用户
    @Select("SELECT * FROM sys_user WHERE role = #{role}")
    List<Yonghu> selectYonghuByRole(String role);

    // 搜索用户
    @Select("SELECT * FROM sys_user WHERE username LIKE CONCAT('%', #{keyword}, '%')")
    List<Yonghu> searchYonghu(String keyword);

    // 仅保留获取用户总数的方法
    @Select("SELECT COUNT(*) FROM sys_user")
    int countAllUsers();
}