package com.example.airplanesalehouduan.passengers.controller;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 天气查询控制器
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final String API_KEY = "558b89d2a351f53c39d940f76f602189";
    private static final String API_URL = "http://apis.juhe.cn/simpleWeather/query";

    @GetMapping("/query")
    public ResponseEntity<ApiResponse<?>> queryWeather(@RequestParam String city) {
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("key", API_KEY);
            map.put("city", city);

            URL url = new URL(String.format("%s?%s", API_URL, params(map)));
            BufferedReader in = new BufferedReader(new InputStreamReader((url.openConnection()).getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 解析返回的JSON字符串为Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.readValue(response.toString(), Map.class);

            // 检查聚合数据的错误码
            if (result.containsKey("error_code") && (Integer) result.get("error_code") != 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("查询失败: " + result.get("reason")));
            }

            return ResponseEntity.ok(ApiResponse.success("查询成功", result));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(ApiResponse.error("查询天气失败: " + e.getMessage()));
        }
    }

    private String params(Map<String, String> map) {
        return map.entrySet().stream()
                .map(entry -> {
                    try {
                        return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("&"));
    }
}
