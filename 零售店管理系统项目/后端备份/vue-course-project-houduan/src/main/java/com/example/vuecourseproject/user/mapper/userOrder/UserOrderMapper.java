package com.example.vuecourseproject.user.mapper.userOrder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.vuecourseproject.user.entity.userOrder.UserOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserOrderMapper extends BaseMapper<UserOrder> {
    // 不需要额外方法，保持原有逻辑
}