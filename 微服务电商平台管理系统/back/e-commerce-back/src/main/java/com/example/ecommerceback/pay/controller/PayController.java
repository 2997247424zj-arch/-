package com.example.ecommerceback.pay.controller;

import com.example.ecommerceback.pay.service.PayService;
import com.example.ecommerceback.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody Map<String, Object> data) {
        String orderNo = data.get("orderNo") == null ? null : data.get("orderNo").toString();
        Integer payType = parsePayType(data.get("payType"), data.get("payMethod"));
        return payService.create(orderNo, payType);
    }

    @PostMapping("/notify")
    public Result<Void> notify(
        @RequestBody(required = false) Map<String, Object> data,
        @RequestParam(required = false) String payNo,
        @RequestParam(required = false) Boolean success
    ) {
        String targetPayNo = payNo;
        Boolean targetSuccess = success;

        if (data != null) {
            if (targetPayNo == null && data.get("payNo") != null) {
                targetPayNo = data.get("payNo").toString();
            }
            if (targetSuccess == null && data.get("status") != null) {
                targetSuccess = "success".equalsIgnoreCase(data.get("status").toString())
                    || "paid".equalsIgnoreCase(data.get("status").toString())
                    || "1".equals(data.get("status").toString());
            }
        }

        if (targetPayNo == null || targetSuccess == null) {
            return Result.error(400, "支付回调参数不完整");
        }
        return payService.notify(targetPayNo, targetSuccess);
    }

    private Integer parsePayType(Object payType, Object payMethod) {
        if (payType != null) {
            return Integer.valueOf(payType.toString());
        }
        if (payMethod == null) {
            return 1;
        }
        String method = payMethod.toString().toLowerCase();
        return "wechat".equals(method) ? 2 : 1;
    }
}
