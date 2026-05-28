package com.example.airplanesalehouduan.operationsStaff.controller;

import com.example.airplanesalehouduan.operationsStaff.entity.OpFlightException;
import com.example.airplanesalehouduan.operationsStaff.service.OpExceptionsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/op/operations/exceptions")
public class OpExceptionsController {

    private final OpExceptionsService exceptionsService;

    public OpExceptionsController(OpExceptionsService exceptionsService) {
        this.exceptionsService = exceptionsService;
    }

    /**
     * 获取最近上报的异常（默认 20 条）
     */
    @GetMapping
    public Map<String, Object> listExceptions(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        List<OpFlightException> list = exceptionsService.listRecentExceptions(page, size);
        long total = exceptionsService.countExceptions();
        Map<String, Object> data = new HashMap<>();
        data.put("exceptions", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", data);
        return resp;
    }

    /**
     * 更新异常处理状态（运营处置）
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = body.get("status") != null ? String.valueOf(body.get("status")) : null;
        String operatorNote = body.get("operatorNote") != null ? String.valueOf(body.get("operatorNote")) : null;
        Map<String, Object> resp = new HashMap<>();
        if (status == null || status.isBlank()) {
            resp.put("success", false);
            resp.put("message", "缺少 status 参数");
            return resp;
        }
        var opt = exceptionsService.updateExceptionStatus(id, status, operatorNote);
        if (opt.isPresent()) {
            resp.put("success", true);
            resp.put("data", opt.get());
        } else {
            resp.put("success", false);
            resp.put("message", "未找到对应的异常记录");
        }
        return resp;
    }
}


