package com.example.ecommerceback.log.controller;

import com.example.ecommerceback.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 日志控制器
 * 用于接收前端日志
 */
@Slf4j
@RestController
@RequestMapping("/api/log")
public class LogController {

    @PostMapping
    public Result<String> log(@RequestBody Map<String, Object> logData) {
        try {
            String level = (String) logData.getOrDefault("level", "info");
            String message = (String) logData.getOrDefault("message", "");
            
            switch (level.toLowerCase()) {
                case "error":
                    log.error("前端日志: {}", message);
                    break;
                case "warn":
                    log.warn("前端日志: {}", message);
                    break;
                default:
                    log.info("前端日志: {}", message);
            }
            
            return Result.success("日志已记录");
        } catch (Exception e) {
            log.error("记录前端日志失败", e);
            return Result.error(500, "记录日志失败");
        }
    }
}
