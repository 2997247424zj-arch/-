package com.example.vuecourseproject.user.controller.echart;


import com.example.vuecourseproject.user.other.echart.UserExpenseStatsDTO;
import com.example.vuecourseproject.user.other.password.Result;
import com.example.vuecourseproject.user.service.echart.UserOrderStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/stats")
public class UserOrderStatsController {

    @Autowired
    private UserOrderStatsService userOrderStatsService;

    /**
     * 获取用户按月支出统计
     * @param username 用户名
     * @return 统计结果
     */
    @GetMapping("/monthly/{username}")
    public Result<List<UserExpenseStatsDTO>> getMonthlyStats(@PathVariable String username) {
        List<UserExpenseStatsDTO> stats = userOrderStatsService.getUserMonthlyExpenseStats(username);
        return Result.success(stats);
    }

    /**
     * 获取用户按年支出统计
     * @param username 用户名
     * @return 统计结果
     */
    @GetMapping("/yearly/{username}")
    public Result<List<UserExpenseStatsDTO>> getYearlyStats(@PathVariable String username) {
        List<UserExpenseStatsDTO> stats = userOrderStatsService.getUserYearlyExpenseStats(username);
        return Result.success(stats);
    }
}
