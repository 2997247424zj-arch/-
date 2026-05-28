package com.example.airplanesalehouduan.sms;

import com.example.airplanesalehouduan.Login.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth/sms")
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private SmsService smsService;

    /**
     * 发送短信验证码
     * 请求体示例：{"phone":"13800138000"}
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<?>> sendSms(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("手机号不能为空"));
        }
        try {
            boolean ok = smsService.sendVerifyCode(phone);
            if (ok) {
                return ResponseEntity.ok(ApiResponse.success("短信发送请求已发出", null));
            } else {
                // 不应通常到达此分支，但保留兼容处理
                return ResponseEntity.status(500).body(ApiResponse.error("短信发送失败，请检查阿里云配置"));
            }
        } catch (Exception e) {
            // 暂时把异常信息回传，便于调试（生产可改为不泄露内部错误）
            logger.error("Error sending sms to {}: {}", phone, e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("短信发送异常: " + e.getMessage()));
        }
    }

    /**
     * 校验短信验证码
     * 请求体示例：{"phone":"13800138000","code":"123456"}
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifySms(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        if (phone == null || code == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("手机号或验证码不能为空"));
        }
        boolean ok = smsService.checkVerifyCode(phone, code);
        if (ok) {
            return ResponseEntity.ok(ApiResponse.success("验证码校验通过", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("验证码校验失败"));
        }
    }
}


