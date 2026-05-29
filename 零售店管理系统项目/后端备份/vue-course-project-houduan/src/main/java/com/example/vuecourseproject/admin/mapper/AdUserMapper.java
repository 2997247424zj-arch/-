package com.example.vuecourseproject.admin.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.*;

@Mapper
public interface AdUserMapper {
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteUserById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM sys_user WHERE id = #{id}")
    int checkUserExists(@Param("id") Long id);
}