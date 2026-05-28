package com.example.airplanesalehouduan.passengers.controller;


import com.example.airplanesalehouduan.Login.dto.ApiResponse;

import com.example.airplanesalehouduan.passengers.other.FlightSearchRequest;
import com.example.airplanesalehouduan.passengers.service.FlightService;
import com.example.airplanesalehouduan.passengers.service.BookingService;
import com.example.airplanesalehouduan.passengers.service.TicketDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 航班控制器
 * 处理航班相关的API请求
 */
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TicketDocumentService ticketDocumentService;

    /**
     * 搜索航班接口
     * GET /api/flights/search?departure=北京&destination=上海&date=2024-01-15&passengers=1&class=economy
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchFlights(
            @RequestParam String departure,
            @RequestParam String destination,
            @RequestParam String date,
            @RequestParam(required = false) Integer passengers,
            @RequestParam(value = "class", required = false) String cabinClass) {

        try {
            // 构建搜索请求
            FlightSearchRequest request = new FlightSearchRequest();
            request.setDeparture(departure);
            request.setDestination(destination);
            request.setDate(date);
            request.setPassengers(passengers);
            request.setCabinClass(cabinClass);

            // 执行搜索
            List<Map<String, Object>> flights = flightService.searchFlights(request);

            // 构建响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("flights", flights);
            responseData.put("total", flights.size());

            return ResponseEntity.ok(ApiResponse.success("搜索成功", responseData));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索航班失败：" + e.getMessage()));
        }
    }

    /**
     * 预订航班接口
     * POST /api/flights/book
     * 请求体：
     * {
     *   "flightId": 1,
     *   "passengerId": 3,
     *   "passengers": [
     *     {
     *       "name": "张三",
     *       "idCard": "110101199001011234",
     *       "phone": "13800138000",
     *       "seatNumber": "10A",
     *       "seatClass": "economy",
     *       "seatFee": 200.00
     *     }
     *   ],
     *   "totalAmount": 880.00,
     *   "usedPoints": 100,
     *   "appliedCoupons": [
     *     {
     *       "id": 1,
     *       "name": "新用户优惠券",
     *       "type": "cash",
     *       "appliedAmount": 50.00
     *     }
     *   ]
     * }
     */
    @PostMapping("/book")
    public ResponseEntity<ApiResponse<?>> bookFlight(@RequestBody Map<String, Object> request) {
        try {
            // 解析请求参数
            Integer flightId = Integer.valueOf(request.get("flightId").toString());
            Integer passengerId = Integer.valueOf(request.get("passengerId").toString());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> passengersData = (List<Map<String, Object>>) request.get("passengers");
            if (passengersData == null || passengersData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("乘客信息不能为空"));
            }

            // 解析总金额
            BigDecimal totalAmount = new BigDecimal(request.get("totalAmount").toString());

            // 解析使用的积分（可选）
            Integer usedPoints = null;
            if (request.containsKey("usedPoints") && request.get("usedPoints") != null) {
                usedPoints = Integer.valueOf(request.get("usedPoints").toString());
            }

            // 解析优惠券信息（可选）
            List<BookingService.CouponInfo> appliedCoupons = null;
            if (request.containsKey("appliedCoupons") && request.get("appliedCoupons") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> couponsData = (List<Map<String, Object>>) request.get("appliedCoupons");
                appliedCoupons = new ArrayList<>();
                for (Map<String, Object> couponData : couponsData) {
                    BookingService.CouponInfo coupon = new BookingService.CouponInfo();
                    if (couponData.containsKey("id")) {
                        coupon.setId(Long.valueOf(couponData.get("id").toString()));
                    }
                    if (couponData.containsKey("name")) {
                        coupon.setName(couponData.get("name").toString());
                    }
                    if (couponData.containsKey("type")) {
                        coupon.setType(couponData.get("type").toString());
                    }
                    if (couponData.containsKey("appliedAmount")) {
                        coupon.setAppliedAmount(Double.valueOf(couponData.get("appliedAmount").toString()));
                    }
                    appliedCoupons.add(coupon);
                }
            }

            // 转换乘客信息
            List<BookingService.PassengerInfo> passengers = new ArrayList<>();
            for (Map<String, Object> passengerData : passengersData) {
                BookingService.PassengerInfo passenger = new BookingService.PassengerInfo();
                passenger.setName(passengerData.get("name").toString());
                passenger.setIdCard(passengerData.get("idCard").toString());
                if (passengerData.containsKey("phone") && passengerData.get("phone") != null) {
                    passenger.setPhone(passengerData.get("phone").toString());
                }
                if (passengerData.containsKey("seatNumber") && passengerData.get("seatNumber") != null) {
                    passenger.setSeatNumber(passengerData.get("seatNumber").toString());
                }
                if (passengerData.containsKey("seatClass") && passengerData.get("seatClass") != null) {
                    passenger.setSeatClass(passengerData.get("seatClass").toString());
                }
                if (passengerData.containsKey("seatFee") && passengerData.get("seatFee") != null) {
                    passenger.setSeatFee(new BigDecimal(passengerData.get("seatFee").toString()));
                }
                passengers.add(passenger);
            }

            // 调用预订服务
            com.example.airplanesalehouduan.passengers.entity.Order order =
                    bookingService.bookFlight(passengerId, flightId, passengers, totalAmount, usedPoints, appliedCoupons);

            // 构建响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("orderId", order.getId());
            responseData.put("orderNo", order.getOrderNo());
            responseData.put("ticketNo", order.getTicketNo()); // 返回所有机票号（逗号分隔）
            responseData.put("message", "预订成功");

            return ResponseEntity.ok(ApiResponse.success("预订成功", responseData));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("参数格式错误：" + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("预订失败：" + e.getMessage()));
        }
    }

    /**
     * 下载机票文档
     * GET /api/flights/ticket/{ticketNo}/document
     */
    @GetMapping("/ticket/{ticketNo}/document")
    public ResponseEntity<byte[]> downloadTicketDocument(@PathVariable String ticketNo) {
        try {
            byte[] documentBytes = ticketDocumentService.generateTicketDocument(ticketNo);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "机票_" + ticketNo + ".docx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(documentBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 下载订单下所有机票文档（ZIP格式）
     * GET /api/flights/order/{orderNo}/documents
     */
    @GetMapping("/order/{orderNo}/documents")
    public ResponseEntity<?> downloadOrderDocuments(@PathVariable String orderNo) {
        try {
            System.out.println("收到下载文档请求，订单号: " + orderNo);
            byte[] zipBytes = ticketDocumentService.generateTicketDocumentsByOrderNo(orderNo);
            System.out.println("文档生成成功，大小: " + zipBytes.length + " bytes");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "订单_" + orderNo + "_机票文档.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipBytes);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("生成文档IO异常: " + e.getMessage());
            // 对于文件下载接口，如果出错，返回JSON错误信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(ApiResponse.error("生成文档失败：" + e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.err.println("生成文档运行时异常: " + e.getMessage());
            // 对于文件下载接口，如果出错，返回JSON错误信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .headers(headers)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("生成文档未知异常: " + e.getMessage());
            // 对于文件下载接口，如果出错，返回JSON错误信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .headers(headers)
                    .body(ApiResponse.error("下载失败：" + e.getMessage()));
        }
    }
}


