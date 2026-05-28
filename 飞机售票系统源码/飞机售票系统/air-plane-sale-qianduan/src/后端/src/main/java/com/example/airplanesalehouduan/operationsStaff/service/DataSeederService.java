package com.example.airplanesalehouduan.operationsStaff.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 自动数据填充服务（仅供测试/演示）
 *
 * - 每天定时向主要表插入随机但合理的记录，尽量填充每个字段以便统计/展示使用。
 * - 使用 JdbcTemplate 直接插入，遵守外键顺序：users -> aircraft_types -> flights -> seats -> orders -> tickets -> 其它依赖表
 *
 * 注意：在生产环境请禁用此服务（例如通过配置关闭）。
 */
@Service
public class DataSeederService {

    private final JdbcTemplate jdbcTemplate;
    private final Random rand = new Random();

    public DataSeederService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 默认每天 03:05 执行（可通过配置覆盖 data.seed.cron）
    @Scheduled(cron = "${data.seed.cron:0 5 3 * * ?}")
    public void seedDaily() {
        try {
            System.out.println("DataSeederService: starting daily seed at " + LocalDateTime.now());
            // 插入一组用户
            int usersToCreate = 3;
            List<Integer> userIds = new ArrayList<>();
            for (int i = 0; i < usersToCreate; i++) {
                userIds.add(createUser());
            }

            // 插入机型与航班
            int aircraftId = createAircraftType();
            List<Integer> flightIds = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                flightIds.add(createFlight(aircraftId));
            }

            // 每个航班插入若干座位
            for (Integer fid : flightIds) {
                createSeatsForFlight(fid, 8); // 8 seats per flight for testing
            }

            // 插入订单与机票、行李等
            for (int i = 0; i < 5; i++) {
                int userId = userIds.get(rand.nextInt(userIds.size()));
                String orderNo = createOrder(userId, flightIds.get(rand.nextInt(flightIds.size())));
                createTicket(userId, orderNo);
                createBaggage(orderNo, userId);
                createLoyaltyPoints(userId, orderNo);
                createCoupon(userId);
                createFrequentPassenger(userId);
            }

            // 创建改签/取消申请示例
            if (!userIds.isEmpty()) {
                createTicketChangeRequest(userIds.get(0));
                createTicketCancelRequest(userIds.get(0));
            }

            // 插入异常与特殊服务
            if (!flightIds.isEmpty()) {
                createFlightException(flightIds.get(0));
            }
            if (!userIds.isEmpty()) {
                createSpecialServiceRequest(userIds.get(0));
            }

            System.out.println("DataSeederService: daily seed finished");
        } catch (Exception e) {
            System.err.println("DataSeederService.seedDaily error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- helper methods ---
    private int createUser() {
        try {
            String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
            String password = "pass123"; // hashed not required for test
            String idCard = String.valueOf(100000000000000000L + Math.abs(rand.nextLong()) % 899999999999999999L).substring(0,18);
            String phone = "13" + (100000000 + rand.nextInt(900000000));
            String realName = "测试用户" + (rand.nextInt(9000) + 1000);

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update((Connection conn) -> {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (username,password,id_card,phone,real_name,role,status,registration_time) VALUES (?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, idCard);
                ps.setString(4, phone);
                ps.setString(5, realName);
                ps.setString(6, "passenger");
                ps.setString(7, "active");
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                return ps;
            }, keyHolder);
            Number id = keyHolder.getKey();
            return id == null ? 0 : id.intValue();
        } catch (Exception e) {
            System.err.println("createUser failed: " + e.getMessage());
            return 0;
        }
    }

    private int createAircraftType() {
        try {
            String code = "A320-" + (rand.nextInt(900) + 100);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update((Connection conn) -> {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO aircraft_types (type_code,manufacturer,model,seat_layout,status) VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, code);
                ps.setString(2, "Boeing");
                ps.setString(3, "Model-" + (rand.nextInt(100)+1));
                ps.setString(4, "{\"layout\":\"2-3-2\"}");
                ps.setString(5, "active");
                return ps;
            }, keyHolder);
            Number id = keyHolder.getKey();
            return id == null ? 0 : id.intValue();
        } catch (Exception e) {
            System.err.println("createAircraftType failed: " + e.getMessage());
            return 0;
        }
    }

    private int createFlight(int aircraftTypeId) {
        try {
            String flightNo = "FL" + (1000 + rand.nextInt(9000));
            String[] airports = {"PEK","PVG","SHA","CAN","SZX","TAO","CSX","CKG","HGH","KMG"};
            String origin = airports[rand.nextInt(airports.length)];
            String dest;
            do { dest = airports[rand.nextInt(airports.length)]; } while (dest.equals(origin));
            LocalDateTime dep = LocalDateTime.now().plusDays(rand.nextInt(10)).withHour(8 + rand.nextInt(10)).withMinute(0);
            LocalDateTime arr = dep.plusHours(1 + rand.nextInt(5));
            double price = Math.round((100 + rand.nextDouble()*1000) * 100.0) / 100.0;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            String finalDest = dest;
            jdbcTemplate.update((Connection conn) -> {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO flights (flight_no,aircraft_type_id,origin_airport,dest_airport,sched_dep_time,sched_arr_time,status,price,route_info,created_at) VALUES (?,?,?,?,?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, flightNo);
                ps.setInt(2, aircraftTypeId);
                ps.setString(3, origin);
                ps.setString(4, finalDest);
                ps.setTimestamp(5, Timestamp.valueOf(dep));
                ps.setTimestamp(6, Timestamp.valueOf(arr));
                ps.setString(7, "scheduled");
                ps.setDouble(8, price);
                ps.setString(9, "{\"distance\":500}");
                ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                return ps;
            }, keyHolder);
            Number id = keyHolder.getKey();
            return id == null ? 0 : id.intValue();
        } catch (Exception e) {
            System.err.println("createFlight failed: " + e.getMessage());
            return 0;
        }
    }

    private void createSeatsForFlight(int flightId, int count) {
        try {
            String[] classes = {"economy","business"};
            for (int i = 1; i <= count; i++) {
                String seatNumber = i + String.valueOf((char)('A' + (i%6)));
                String cabin = classes[i%classes.length];
                double price = cabin.equals("economy") ? 200 + rand.nextInt(200) : 800 + rand.nextInt(400);
                jdbcTemplate.update("INSERT IGNORE INTO seats (flight_id,seat_number,cabin_class,rownumber,seat_position,price,available_count,status,created_at) VALUES (?,?,?,?,?,?,?,?,?)",
                        flightId, seatNumber, cabin, i, seatNumber.replaceAll("\\d",""), price, 1, "available", Timestamp.valueOf(LocalDateTime.now()));
            }
        } catch (Exception e) {
            System.err.println("createSeatsForFlight failed: " + e.getMessage());
        }
    }

    private String createOrder(int passengerId, int flightId) {
        try {
            String orderNo = "ORD" + System.currentTimeMillis() + (rand.nextInt(900)+100);
            String route = jdbcTemplate.queryForObject("SELECT CONCAT(origin_airport,' → ',dest_airport) FROM flights WHERE id = ?", new Object[]{flightId}, String.class);
            LocalDateTime dep = jdbcTemplate.queryForObject("SELECT sched_dep_time FROM flights WHERE id = ?", new Object[]{flightId}, LocalDateTime.class);
            double total = 100 + rand.nextInt(1000);
            jdbcTemplate.update("INSERT INTO orders (passenger_id,order_no,passenger_name,route,flight_no,ticket_no,departure_time,arrival_time,total_amount,status,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    passengerId, orderNo, "旅客" + passengerId, route, null, null, Timestamp.valueOf(dep), Timestamp.valueOf(dep.plusHours(2)), total, "paid", Timestamp.valueOf(LocalDateTime.now()));
            return orderNo;
        } catch (Exception e) {
            System.err.println("createOrder failed: " + e.getMessage());
            return "ORD_FAIL";
        }
    }

    private void createTicket(int passengerId, String orderNo) {
        try {
            // pick a random flight for ticket
            Integer flightId = jdbcTemplate.queryForObject("SELECT id FROM flights ORDER BY RAND() LIMIT 1", Integer.class);
            String flightNo = jdbcTemplate.queryForObject("SELECT flight_no FROM flights WHERE id = ?", new Object[]{flightId}, String.class);
            String ticketNo = "TKT" + System.currentTimeMillis() + rand.nextInt(999);
            jdbcTemplate.update("INSERT INTO tickets (order_no,passenger_id,passenger_name,id_card,phone,flight_id,flight_no,origin_airport,dest_airport,route,departure_time,arrival_time,seat_number,seat_class,base_price,seat_fee,discount_fee,total_price,ticket_no,status,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    orderNo, passengerId, "旅客" + passengerId, "ID" + passengerId, "13" + (100000000 + rand.nextInt(900000000)), flightId, flightNo,
                    jdbcTemplate.queryForObject("SELECT origin_airport FROM flights WHERE id = ?", new Object[]{flightId}, String.class),
                    jdbcTemplate.queryForObject("SELECT dest_airport FROM flights WHERE id = ?", new Object[]{flightId}, String.class),
                    null,
                    Timestamp.valueOf(LocalDateTime.now().plusDays(rand.nextInt(10))),
                    Timestamp.valueOf(LocalDateTime.now().plusDays(rand.nextInt(10)).plusHours(2)),
                    null, "economy", 200.0, 0.0, 0.0, 200.0, ticketNo, "issued", Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("createTicket failed: " + e.getMessage());
        }
    }

    private void createBaggage(String orderNo, int passengerId) {
        try {
            String baggageNo = "BAG" + System.currentTimeMillis() + rand.nextInt(999);
            jdbcTemplate.update("INSERT INTO baggage_management (baggage_no,passenger_id,passenger_name,orderno,flight_no,route,departure_time,arrival_time_flight,baggage_type,baggage_count,total_weight,weight_limit,dimensions,status,registered_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    baggageNo, passengerId, "旅客"+passengerId, orderNo, null, null, Timestamp.valueOf(LocalDateTime.now()), null, "托运行李", 1, 20.0, 23.0, "55x40x20", "registered", Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("createBaggage failed: " + e.getMessage());
        }
    }

    private void createLoyaltyPoints(int passengerId, String orderNo) {
        try {
            jdbcTemplate.update("INSERT INTO loyalty_points (passenger_id,points,change_type,ref_order_id,remark,created_at) VALUES (?,?,?,?,?,?)",
                    passengerId, 100, "earn", null, "日常积分", Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("createLoyaltyPoints failed: " + e.getMessage());
        }
    }

    private void createCoupon(int passengerId) {
        try {
            String code = "CPN" + UUID.randomUUID().toString().substring(0,6).toUpperCase();
            jdbcTemplate.update("INSERT INTO coupons (code,passenger_id,discount_type,discount_value,min_spend,valid_from,valid_to,status,name) VALUES (?,?,?,?,?,?,?,?,?)",
                    code, passengerId, "fixed", 50.0, 0.0, Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusMonths(1)), "active", "测试券");
        } catch (Exception e) {
            System.err.println("createCoupon failed: " + e.getMessage());
        }
    }

    private void createFrequentPassenger(int passengerId) {
        try {
            jdbcTemplate.update("INSERT INTO frequent_passengers (user_id,name,id_card,relationship,phone,remarks,is_default,status,created_at) VALUES (?,?,?,?,?,?,?,?,?)",
                    passengerId, "常用乘客"+rand.nextInt(1000), "ID" + (rand.nextInt(900000)+100000), "朋友", "13" + (100000000 + rand.nextInt(900000000)), "备注", 0, "active", Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("createFrequentPassenger failed: " + e.getMessage());
        }
    }

    private void createTicketChangeRequest(int passengerId) {
        try {
            String changeNo = "CHG" + System.currentTimeMillis();
            jdbcTemplate.update("INSERT INTO ticket_change_requests (change_no,orderno,passenger_id,applicant_name,request_time,ticket_no,old_flight_no,old_route,old_departure_time,new_flight_no,new_route,new_departure_time,change_fee,fare_diff,status,reason,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    changeNo, "ORD_SAMPLE", passengerId, "申请人", Timestamp.valueOf(LocalDateTime.now()), null, "FL000", "A→B", Timestamp.valueOf(LocalDateTime.now()), "FL001", "A→C", Timestamp.valueOf(LocalDateTime.now().plusDays(1)), 100.0, 50.0, "pending", "测试改签", Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("createTicketChangeRequest failed: " + e.getMessage());
        }
    }

    private void createTicketCancelRequest(int passengerId) {
        try {
            String cancelNo = "CXL" + System.currentTimeMillis();
            jdbcTemplate.update("INSERT INTO ticket_cancel_requests (cancel_no,orderno,passenger_id,applicant_name,request_time,ticket_no,flight_no,route,departure_time,cancel_fee,refund_fare,status,reason,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    cancelNo, "ORD_SAMPLE", passengerId, "申请人", Timestamp.valueOf(LocalDateTime.now()), null, "FL000", "A→B", Timestamp.valueOf(LocalDateTime.now()), 50.0, 150.0, "pending", "测试退票", Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("createTicketCancelRequest failed: " + e.getMessage());
        }
    }

    private void createSpecialServiceRequest(int passengerId) {
        try {
            String orderNo = "ORD_SPECIAL_" + System.currentTimeMillis();
            jdbcTemplate.update("INSERT INTO special_service_requests (passenger_id,order_no,phone,passenger_type,departure_airport,arrival_airport,entry_services,exit_services,description,status,created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                    passengerId, orderNo, "13" + (100000000 + rand.nextInt(900000000)), "轮椅", "PEK", "SHA", "{\"wheelchair\":true}", "{\"assistance\":true}", "需要帮助", "pending", Timestamp.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.err.println("createSpecialServiceRequest failed: " + e.getMessage());
        }
    }

    private void createFlightException(int flightId) {
        try {
            String exNo = "EX" + System.currentTimeMillis();
            jdbcTemplate.update("INSERT INTO flight_exceptions (exception_no,flight_id,flight_no,exception_type,reported_by,reported_at,reason,delay_minutes,status,operator_note) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    exNo, flightId, jdbcTemplate.queryForObject("SELECT flight_no FROM flights WHERE id = ?", new Object[]{flightId}, String.class),
                    "delay", null, Timestamp.valueOf(LocalDateTime.now()), "天气原因", 30, "reported", "自动插入测试异常");
        } catch (Exception e) {
            System.err.println("createFlightException failed: " + e.getMessage());
        }
    }
}


