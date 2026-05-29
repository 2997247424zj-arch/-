package com.example.vuecourseproject.user.other.address;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@RestControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true; // 仍然支持所有接口，通过 beforeBodyWrite 过滤
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();

        // 1. 不包装的接口：登录接口 + 已经手动包装的 ResponseEntity 接口
        if (path.contains("/api/auth/login") || body instanceof ResponseEntity) {
            return body;
        }

        // 2. 只对 /api/addresses 相关接口包装
        if (path.startsWith("/api/addresses")) {
            return Map.of("code", 200, "data", body, "message", "success");
        }

        // 3. 其他接口返回原始数据
        return body;
    }
}