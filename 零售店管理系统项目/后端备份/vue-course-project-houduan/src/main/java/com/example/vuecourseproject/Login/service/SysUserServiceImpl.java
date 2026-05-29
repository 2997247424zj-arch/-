package com.example.vuecourseproject.Login.service;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vuecourseproject.Login.entity.SysUser;
import com.example.vuecourseproject.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Override
    public SysUser getByUsername(String username) {
        // 使用MyBatis-Plus的Lambda查询
        return lambdaQuery()
                .eq(SysUser::getUsername, username)
                .one();
    }
}
