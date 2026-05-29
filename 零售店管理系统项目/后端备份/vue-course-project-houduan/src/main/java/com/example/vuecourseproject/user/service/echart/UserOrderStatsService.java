package com.example.vuecourseproject.user.service.echart;


import com.example.vuecourseproject.user.mapper.echart.EchartUserOrderMapper;
import com.example.vuecourseproject.user.other.echart.UserExpenseStatsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserOrderStatsService {

    @Autowired
    private EchartUserOrderMapper userOrderMapper;

    /**
     * 获取用户按月支出统计
     * @param username 用户名
     * @return 按月统计结果
     */
    public List<UserExpenseStatsDTO> getUserMonthlyExpenseStats(String username) {
        return userOrderMapper.getMonthlyExpenseStats(username);
    }

    /**
     * 获取用户按年支出统计
     * @param username 用户名
     * @return 按年统计结果
     */
    public List<UserExpenseStatsDTO> getUserYearlyExpenseStats(String username) {
        return userOrderMapper.getYearlyExpenseStats(username);
    }
}