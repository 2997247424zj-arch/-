package com.example.musicshareplatformback.user.controller;

import com.example.musicshareplatformback.common.ApiResponse;
import com.example.musicshareplatformback.user.dto.UserDashboardResponse;
import com.example.musicshareplatformback.user.service.UserDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserDashboardController {

    private final UserDashboardService userDashboardService;

    public UserDashboardController(UserDashboardService userDashboardService) {
        this.userDashboardService = userDashboardService;
    }

    @GetMapping("/{userId}/dashboard")
    public ApiResponse<UserDashboardResponse> getDashboard(@PathVariable String userId) {
        return ApiResponse.success("User dashboard loaded.", userDashboardService.getDashboard(userId));
    }
}
