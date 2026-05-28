package com.example.musicshareplatformback.home.controller;

import com.example.musicshareplatformback.common.ApiResponse;
import com.example.musicshareplatformback.home.dto.HomePageResponse;
import com.example.musicshareplatformback.home.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    public ApiResponse<HomePageResponse> getHomePage() {
        return ApiResponse.success("Homepage data loaded.", homeService.getHomePage());
    }
}
