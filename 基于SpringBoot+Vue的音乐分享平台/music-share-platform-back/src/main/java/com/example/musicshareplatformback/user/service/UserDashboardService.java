package com.example.musicshareplatformback.user.service;

import com.example.musicshareplatformback.user.dto.UserDashboardResponse;

public interface UserDashboardService {

    UserDashboardResponse getDashboard(String userId);
}
