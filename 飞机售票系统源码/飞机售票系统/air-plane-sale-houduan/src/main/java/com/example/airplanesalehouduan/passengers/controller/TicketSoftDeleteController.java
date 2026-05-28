package com.example.airplanesalehouduan.passengers.controller;

import com.example.airplanesalehouduan.passengers.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 机票软删除控制器
 * 提供机票软删除功能，将机票状态设置为"deleted"而不是物理删除
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/passenger/tickets")
public class TicketSoftDeleteController {

    @Autowired
    private TicketService ticketService;

    /**
     * 软删除机票
     * 将机票状态更新为"deleted"，而不是物理删除
     */
    @DeleteMapping("/{ticketId}/soft-delete")
    public ResponseEntity<Map<String, Object>> softDeleteTicket(
            @PathVariable("ticketId") Long ticketId,
            @RequestParam("passengerId") Integer passengerId) {

        try {
            // 验证参数
            if (ticketId == null || passengerId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "机票ID和乘客ID不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 执行软删除
            boolean result = ticketService.softDeleteTicket(ticketId, passengerId);

            if (result) {
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "机票已成功软删除");
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "软删除失败，可能机票不存在或不属于当前乘客");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "软删除过程中发生错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 根据机票号软删除机票
     * 将机票状态更新为"deleted"，而不是物理删除
     */
    @DeleteMapping("/soft-delete-by-ticket-no")
    public ResponseEntity<Map<String, Object>> softDeleteTicketByTicketNo(
            @RequestParam("ticketNo") String ticketNo,
            @RequestParam("passengerId") Integer passengerId) {

        try {
            // 验证参数
            if (ticketNo == null || ticketNo.trim().isEmpty() || passengerId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "机票号和乘客ID不能为空");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 执行软删除
            boolean result = ticketService.softDeleteTicketByTicketNo(ticketNo.trim(), passengerId);

            if (result) {
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "机票已成功软删除");
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "软删除失败，可能机票不存在或不属于当前乘客");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "软删除过程中发生错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}