/*
 Navicat Premium Data Transfer

 Source Server         : 课程设计
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : flight

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 05/01/2026 16:09:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for aircraft_type_requests
-- ----------------------------
DROP TABLE IF EXISTS `aircraft_type_requests`;
CREATE TABLE `aircraft_type_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '拟申报机型代码，如A321XLR',
  `max_seats` int NULL DEFAULT NULL COMMENT '最大座位数',
  `cabin_mix` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '舱位组合描述，如 商务16/经济180',
  `compatible_airlines` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适配航司/运营方列表',
  `base_airports` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '计划投放基地，逗号分隔',
  `maintenance_cycle_months` int NULL DEFAULT NULL COMMENT '建议维护周期（月）',
  `status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `submitted_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `processed_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pending_code`(`type_code` ASC, `status` ASC) USING BTREE,
  INDEX `idx_status_time`(`status` ASC, `submitted_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '新增机型申报与审批' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of aircraft_type_requests
-- ----------------------------
INSERT INTO `aircraft_type_requests` VALUES (1, 'A321XLR', 220, '商务12/经济208', '中国国际航空,中国东方航空,中国南方航空,海南航空', '北京首都,上海浦东,广州白云,深圳宝安', 18, '待处理', '2025-12-15 19:50:42', NULL);

-- ----------------------------
-- Table structure for aircraft_types
-- ----------------------------
DROP TABLE IF EXISTS `aircraft_types`;
CREATE TABLE `aircraft_types`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `type_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `manufacturer` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `seat_layout` json NULL,
  `status` enum('active','retired') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'active',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_code`(`type_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '机型管理' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of aircraft_types
-- ----------------------------
INSERT INTO `aircraft_types` VALUES (1, 'B737-800', 'Boeing', '737-800', '{\"layout\": \"Y160\"}', 'active');
INSERT INTO `aircraft_types` VALUES (6, 'A320', '中国国际航空', 'A320-200', '{\"商务舱\": 8, \"头等舱\": 36, \"经济舱\": 102}', 'active');
INSERT INTO `aircraft_types` VALUES (7, 'B737', '中国南方航空', '737-800', '{\"商务舱\": 12, \"头等舱\": 48, \"经济舱\": 54}', 'active');
INSERT INTO `aircraft_types` VALUES (8, 'A333', '吉祥航空(中国南方航空)', 'A330-300', '{\"商务舱\": 8, \"头等舱\": 36, \"经济舱\": 36}', 'active');
INSERT INTO `aircraft_types` VALUES (9, 'B788', '中国国际航空', '787-8 梦想客机', '{\"商务舱\": 24, \"头等舱\": 8, \"经济舱\": 162}', 'active');
INSERT INTO `aircraft_types` VALUES (10, 'AR21', '吉祥航空(中国东方航空)', 'ARJ21-700', '{\"商务舱\": 16, \"头等舱\": 8, \"经济舱\": 36}', 'active');
INSERT INTO `aircraft_types` VALUES (12, 'A111', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (13, 'A111-100', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (14, 'A111-001', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (15, 'A111-002', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (16, 'A111-003', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (17, 'A111-004', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (18, 'A111-005', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (19, 'A111-005-001', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (20, 'A111-005-002', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (21, 'A223344', '中国商飞', '666-777', '{\"商务舱\": 12, \"头等舱\": 48, \"经济舱\": 54}', 'active');
INSERT INTO `aircraft_types` VALUES (22, 'AJ123', '中国商飞', 'AJ123-01', '{\"商务舱\": 8, \"头等舱\": 36, \"经济舱\": 36}', 'active');
INSERT INTO `aircraft_types` VALUES (23, 'INTL_A320', 'Airbus', 'A32', '{\"layout\": \"3-3\"}', 'active');
INSERT INTO `aircraft_types` VALUES (24, 'A111-005-002-001', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (25, 'A111-005-002-002', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (26, 'A111-005-002-003', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');
INSERT INTO `aircraft_types` VALUES (27, 'A111-005-002-004', '中国航空', 'AE86-90', '{\"商务舱\": 20, \"头等舱\": 40, \"经济舱\": 120}', 'active');

-- ----------------------------
-- Table structure for baggage_management
-- ----------------------------
DROP TABLE IF EXISTS `baggage_management`;
CREATE TABLE `baggage_management`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `baggage_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行李编号/标识号，用于前端展示',
  `passenger_id` int NOT NULL COMMENT '用户ID/乘客ID，关联users(id)，实现数据隔离',
  `passenger_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '乘客姓名（冗余展示）',
  `orderno` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号，关联orders(order_no)',
  `flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '航班号',
  `route` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '航线（格式：出发地 → 目的地）',
  `departure_time` datetime NOT NULL COMMENT '起飞时间',
  `arrival_time_flight` datetime NULL DEFAULT NULL COMMENT '航班到达时间',
  `baggage_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行李类型（托运行李、随身行李、超规行李、特殊行李等）',
  `baggage_count` int NOT NULL DEFAULT 1 COMMENT '行李数量',
  `total_weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '总重量（公斤）',
  `weight_limit` decimal(10, 2) NULL DEFAULT NULL COMMENT '重量限制（公斤）',
  `dimensions` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '尺寸（长x宽x高，单位：厘米，如：100x60x40）',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行李状态（registered-已登记、checked_in-已托运、in_transit-运输中、arrived-已到达、delivered-已提取、lost-丢失、damaged-损坏、delayed-延误）',
  `registered_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
  `checked_in_time` datetime NULL DEFAULT NULL COMMENT '托运时间',
  `arrival_time_baggage` datetime NULL DEFAULT NULL COMMENT '行李到达时间',
  `delivered_time` datetime NULL DEFAULT NULL COMMENT '提取时间',
  `baggage_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '行李费用',
  `excess_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '超重/超规费用',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '行李描述（颜色、特征等，便于识别）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注信息',
  `processed_by` int NULL DEFAULT NULL COMMENT '运营处理人ID，关联users(id)',
  `processed_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `operator_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '运营备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_baggage_no`(`baggage_no` ASC) USING BTREE,
  INDEX `fk_bm_orders`(`orderno` ASC) USING BTREE,
  INDEX `fk_bm_passengers`(`passenger_id` ASC) USING BTREE,
  INDEX `fk_bm_processeds`(`processed_by` ASC) USING BTREE,
  CONSTRAINT `fk_bm_orders` FOREIGN KEY (`orderno`) REFERENCES `orders` (`order_no`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_bm_passengers` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_bm_processeds` FOREIGN KEY (`processed_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '行李管理表（乘客隔离）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of baggage_management
-- ----------------------------
INSERT INTO `baggage_management` VALUES (5, 'BG20251222F9F1D8', 3, '李好呀呀呀', 'ORD202512221123477849', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 23.00, 20.00, '50*30*40', 'registered', '2025-12-22 11:46:32', NULL, NULL, NULL, 45.00, 45.00, '1111', '1112', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (6, 'BG202512222E0F90', 3, '李好呀呀呀', 'ORD202512221123477849', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '50*30*40', 'registered', '2025-12-22 17:49:32', NULL, NULL, NULL, 15.00, 15.00, '111', '13', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (7, 'BG20251222B47B70', 3, '李好呀呀呀', 'ORD202512221930197043', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, NULL, 'registered', '2025-12-22 19:36:08', NULL, NULL, NULL, 15.00, 15.00, '111', '112', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (8, 'BG2025122246D9AB', 3, '李好呀呀呀', 'ORD202512221930197043', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, NULL, 'registered', '2025-12-22 19:47:05', NULL, NULL, NULL, 15.00, 15.00, '1', '2', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (9, 'BG202512224FBF00', 3, '张建', 'ORD202512221951526981', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '50*30*40', 'registered', '2025-12-22 20:07:02', NULL, NULL, NULL, 15.00, 15.00, '1', '2', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (10, 'BG2025122203A4C0', 3, '李好呀呀呀', 'ORD202512221948189133', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '50*30*40', 'registered', '2025-12-22 20:21:13', NULL, NULL, NULL, 15.00, 15.00, '1', '2', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (11, 'BG20251223EF6C23', 3, '李好呀呀呀', 'ORD202512211035063881', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '50*30*40', 'registered', '2025-12-23 15:59:41', NULL, NULL, NULL, 15.00, 15.00, '1', '2', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (12, 'BG20251223628FE9', 3, '李好呀呀呀', 'ORD202512231607146768', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '50*30*40', 'registered', '2025-12-23 16:16:55', NULL, NULL, NULL, 15.00, 15.00, '1', '12', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (13, 'BG20251223D53905', 3, '李好呀呀呀', 'ORD202512211028298071', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '50*40*30', 'registered', '2025-12-23 16:18:53', NULL, NULL, NULL, 15.00, 15.00, '11', '12', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (14, 'BG20251223916C71', 3, '李好呀呀呀', 'ORD202512231800371558', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, NULL, 'registered', '2025-12-23 18:04:26', NULL, NULL, NULL, 15.00, 15.00, '1', '2', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (15, 'BG202512236EEBC6', 3, '张建', 'ORD202512211024288847', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, NULL, 'registered', '2025-12-23 20:47:54', NULL, NULL, NULL, 15.00, 15.00, '11', '12', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (16, 'BG202512232623FE', 3, '李好呀呀呀', '2025122322001426530508088246', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '1', 'registered', '2025-12-23 21:13:35', NULL, NULL, NULL, 15.00, 15.00, '1', '2', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (17, 'BG20251224B3426C', 3, '周瑾', '2025122422001426530508089548', 'HU7615', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', '2025-12-09 18:00:00', '手提行李', 1, 21.00, 20.00, '50*30*40', 'delivered', '2025-12-24 12:27:25', NULL, '2025-12-25 19:09:03', '2025-12-25 19:10:11', 15.00, 15.00, '111', '12', 3, '2025-12-25 19:10:11', '12');
INSERT INTO `baggage_management` VALUES (18, 'BG202512253B0CB4', 3, '李好呀3', '2025122522001426530508111952', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '50*30*40', 'registered', '2025-12-25 19:11:07', NULL, NULL, NULL, 15.00, 15.00, '1', '2', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (19, 'BG2025122763880E', 3, '李好呀3', '2025122722001426530508142492', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '托运行李', 1, 21.00, 20.00, '1', 'registered', '2025-12-27 22:40:18', NULL, NULL, NULL, 15.00, 15.00, '2', '3', NULL, NULL, NULL);
INSERT INTO `baggage_management` VALUES (20, 'BG2025122859F5B6', 3, '李好呀3', 'ORD20251227224406449', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '手提行李', 1, 21.00, 20.00, '50', 'lost', '2025-12-28 21:11:55', '2026-01-04 16:21:49', NULL, NULL, 15.00, 15.00, '1', '2', 2, '2026-01-04 17:27:24', NULL);

-- ----------------------------
-- Table structure for coupons
-- ----------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `passenger_id` int NULL DEFAULT NULL,
  `discount_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `discount_value` decimal(10, 2) NOT NULL,
  `min_spend` decimal(10, 2) NULL DEFAULT 0.00,
  `valid_from` datetime NOT NULL,
  `valid_to` datetime NOT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_passenger`(`passenger_id` ASC) USING BTREE,
  CONSTRAINT `fk_coupon_user` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券（乘客隔离或通用）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupons
-- ----------------------------
INSERT INTO `coupons` VALUES (43, 'WELCOME2025', 3, '百分比折扣', 0.90, 0.00, '2025-12-01 00:00:00', '2026-01-31 23:59:59', 'AVAILABLE', '新用户专享全场9折优惠', '新客专享券');
INSERT INTO `coupons` VALUES (44, 'USER003VIP', 3, '固定金额', 20.00, 50.00, '2025-12-01 00:00:00', '2026-01-12 23:59:59', 'AVAILABLE', '满50元减20元专属优惠', '会员专享券');
INSERT INTO `coupons` VALUES (45, 'XMAS2025', 3, '固定金额', 15.00, 100.00, '2025-12-13 00:00:00', '2026-01-27 23:59:59', 'AVAILABLE', '圣诞促销满100减15', '圣诞礼包');
INSERT INTO `coupons` VALUES (46, 'YEAREND25', 3, '百分比折扣', 0.95, 300.00, '2025-12-01 00:00:00', '2026-01-31 23:59:59', 'AVAILABLE', '年末大促8折优惠', '年末尊享券');
INSERT INTO `coupons` VALUES (47, 'WINTER5', 3, '固定金额', 5.00, 10.00, '2025-12-18 00:00:00', '2026-01-30 23:59:59', 'AVAILABLE', '冬日出行满10元减5元', '冬日暖心券');

-- ----------------------------
-- Table structure for flight_exceptions
-- ----------------------------
DROP TABLE IF EXISTS `flight_exceptions`;
CREATE TABLE `flight_exceptions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `exception_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件编号',
  `flight_id` int NOT NULL COMMENT '航班ID（flights.id）',
  `flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '航班号冗余',
  `exception_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型',
  `reported_by` int NULL DEFAULT NULL COMMENT '上报人ID（users.id）',
  `reported_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '原因/描述',
  `delay_minutes` int NULL DEFAULT NULL COMMENT '延误分钟（如适用）',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理状态（reported/processing/resolved）',
  `resolved_at` datetime NULL DEFAULT NULL COMMENT '解决时间',
  `operator_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '运营处理备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flight_exceptions_no`(`exception_no` ASC) USING BTREE,
  INDEX `fk_fe_flight`(`flight_id` ASC) USING BTREE,
  INDEX `fk_fe_reporter`(`reported_by` ASC) USING BTREE,
  CONSTRAINT `fk_fe_flight` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_fe_reporter` FOREIGN KEY (`reported_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2016 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '航班异常/事件记录（用于运营监控与统计）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flight_exceptions
-- ----------------------------
INSERT INTO `flight_exceptions` VALUES (31, 'EX20251226001', 129, 'CA1301', 'weather_delay', 3, '2025-12-26 08:30:00', '目的地机场大雾，能见度不足，航班延误', 120, 'resolved', '2025-12-26 10:15:00', '已安排后续航班，为旅客提供餐食');
INSERT INTO `flight_exceptions` VALUES (32, 'EX20251226002', 130, 'MU5301', 'mechanical_issue', 19, '2025-12-26 09:45:00', '起飞前检查发现引擎故障，需要维修', 180, 'processing', NULL, '正在紧急维修，预计3小时内完成');
INSERT INTO `flight_exceptions` VALUES (33, 'EX20251226003', 131, 'CZ3401', 'passenger_emergency', 2, '2025-12-26 11:20:00', '一名旅客登机后突发心脏病，需要医疗救助', 60, 'resolved', '2025-12-26 12:10:00', '旅客已送医，航班重新安排起飞时间');
INSERT INTO `flight_exceptions` VALUES (34, 'EX20251226004', 132, 'HU7701', 'baggage_delay', 19, '2025-12-26 13:15:00', '行李装载系统故障，部分行李未能及时装载', 45, 'resolved', '2025-12-26 13:50:00', '行李已重新装载完成，航班准备起飞');
INSERT INTO `flight_exceptions` VALUES (35, 'EX20251226005', 128, 'HO1235', 'air_traffic_control', 15, '2025-12-26 15:30:00', '目的地机场流量控制，航班在地面等待', 90, 'reported', NULL, '等待空管进一步通知');
INSERT INTO `flight_exceptions` VALUES (36, 'EX20251226006', 133, '9C8811', 'crew_timeout', 2, '2025-12-26 17:45:00', '前序航班延误导致机组执勤时间超时，需要更换机组', 120, 'processing', NULL, '正在调配备份机组，预计2小时内到位');
INSERT INTO `flight_exceptions` VALUES (2001, 'EX-2001', 1002, 'TEST1002', 'Delay', NULL, '2025-12-26 19:38:29', '天气原因导致延误', 45, 'reported', NULL, NULL);
INSERT INTO `flight_exceptions` VALUES (2002, 'EX-2002', 1103, 'TREND1103', 'Delay', NULL, '2025-12-26 19:41:53', '技术原因', 30, 'resolved', '2025-12-26 20:03:56', 'Handled by operator UI');
INSERT INTO `flight_exceptions` VALUES (2003, 'EX-2003', 1105, 'TREND1105', 'Delay', NULL, '2025-12-26 19:41:53', '管制', 20, 'resolved', '2025-12-26 20:03:56', 'Handled by operator UI');
INSERT INTO `flight_exceptions` VALUES (2004, 'EX-2004', 1111, 'TREND1111', 'Delay', NULL, '2025-12-26 19:41:53', '天气', 50, 'resolved', '2025-12-26 20:03:57', 'Handled by operator UI');
INSERT INTO `flight_exceptions` VALUES (2005, 'EX-2005', 1120, 'TREND1120', 'Delay', NULL, '2025-12-26 19:41:53', '晚到机组', 25, 'resolved', '2026-01-04 15:16:43', 'Handled via UI');
INSERT INTO `flight_exceptions` VALUES (2006, 'EX-20250104001', 128, 'HO1235', 'weather_delay', 2, '2026-01-04 06:30:00', '起飞地雷雨天气，航班延误', 60, 'reported', NULL, NULL);
INSERT INTO `flight_exceptions` VALUES (2007, 'EX-20250104002', 129, 'CA1301', 'mechanical_issue', 3, '2026-01-04 07:45:00', '起飞前发现发动机故障', 120, 'reported', NULL, NULL);
INSERT INTO `flight_exceptions` VALUES (2008, 'EX-20250104003', 130, 'MU5301', 'crew_shortage', 2, '2026-01-04 09:15:00', '机组人员不足，无法正常执飞', 90, 'reported', NULL, NULL);
INSERT INTO `flight_exceptions` VALUES (2009, 'EX-20250104004', 131, 'CZ3401', 'passenger_delay', 15, '2026-01-04 10:30:00', '旅客登机超时，等待安检', 45, 'reported', NULL, NULL);
INSERT INTO `flight_exceptions` VALUES (2010, 'EX-20250104005', 132, 'HU7701', 'air_traffic_control', 3, '2026-01-04 11:50:00', '目的地机场流量控制', 75, 'reported', NULL, NULL);
INSERT INTO `flight_exceptions` VALUES (2011, 'EX-20250104006', 133, '9C8811', 'baggage_issue', 19, '2026-01-04 13:20:00', '行李装载系统故障', 40, 'reported', NULL, NULL);
INSERT INTO `flight_exceptions` VALUES (2012, 'EX-20250104007', 1002, 'TEST1002', 'fuel_shortage', 2, '2026-01-04 14:40:00', '燃油供应不足，需补充', 30, 'reported', NULL, NULL);
INSERT INTO `flight_exceptions` VALUES (2013, 'EX-20250104008', 1103, 'TREND1103', 'weather_delay', 15, '2026-01-04 16:05:00', '目的地机场大雾，能见度低', 85, 'resolved', '2026-01-04 16:20:53', '由运营界面处理');
INSERT INTO `flight_exceptions` VALUES (2014, 'EX-20250104009', 1105, 'TREND1105', 'technical_issue', 3, '2026-01-04 17:25:00', '导航系统故障', 55, 'resolved', '2026-01-04 17:22:33', '由运营界面处理');
INSERT INTO `flight_exceptions` VALUES (2015, 'EX-20250104010', 1111, 'TREND1111', 'security_check', 2, '2026-01-04 18:45:00', '安检程序延长', 25, 'resolved', '2026-01-04 16:13:05', 'Handled via UI');

-- ----------------------------
-- Table structure for flight_plans
-- ----------------------------
DROP TABLE IF EXISTS `flight_plans`;
CREATE TABLE `flight_plans`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `flight_id` int NOT NULL,
  `plan_date` date NOT NULL,
  `gate` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `runway` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `crew_info` json NULL,
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flight_plan`(`flight_id` ASC, `plan_date` ASC) USING BTREE,
  CONSTRAINT `fk_plan_flight` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '今日航班计划 / 运行计划' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of flight_plans
-- ----------------------------
INSERT INTO `flight_plans` VALUES (16, 11, '2025-12-09', 'A12', '36R', '{\"captain\": \"张伟\", \"copilot\": \"李静\", \"attendants\": [\"王芳\", \"刘洋\"], \"crew_count\": 4}', '准点', '2025-12-09 16:58:44', '2025-12-09 16:58:44');
INSERT INTO `flight_plans` VALUES (17, 12, '2025-12-09', 'B08', '18L', '{\"captain\": \"李明\", \"copilot\": \"陈芳\", \"attendants\": [\"刘涛\", \"赵静\"], \"crew_count\": 4}', '准点', '2025-12-09 16:58:44', '2025-12-09 16:58:44');
INSERT INTO `flight_plans` VALUES (18, 13, '2025-12-09', 'C16', '19L', '{\"captain\": \"王强\", \"copilot\": \"孙丽\", \"attendants\": [\"周明\", \"吴霞\"], \"crew_count\": 4}', '取消', '2025-12-09 16:58:44', '2025-12-09 16:58:44');
INSERT INTO `flight_plans` VALUES (19, 14, '2025-12-09', 'C15', '20L', '{\"captain\": \"陈刚\", \"copilot\": \"李娜\", \"attendants\": [\"郑浩\", \"王敏\"], \"crew_count\": 4}', '取消', '2025-12-09 16:58:44', '2025-12-09 16:58:44');
INSERT INTO `flight_plans` VALUES (20, 15, '2025-12-09', 'D22', '27', '{\"captain\": \"刘伟\", \"copilot\": \"张敏\", \"attendants\": [\"陈杰\", \"李静\", \"王鹏\"], \"crew_count\": 5}', '延误', '2025-12-09 16:58:44', '2025-12-09 16:58:44');
INSERT INTO `flight_plans` VALUES (36, 127, '2025-12-25', 'F11', '30L', '{\"captain\": \"陈军\", \"copilot\": \"赵芳\", \"attendants\": [\"张丽\", \"李浩\"], \"crew_count\": 5}', '准点', '2025-12-26 18:19:40', '2025-12-26 18:19:40');
INSERT INTO `flight_plans` VALUES (37, 129, '2025-12-26', 'A14', '11R', '{\"captain\": \"赵刚\", \"copilot\": \"陈静\", \"attendants\": [\"周婷\", \"王浩\"], \"crew_count\": 5}', '延误', '2025-12-26 18:19:40', '2025-12-26 18:19:40');
INSERT INTO `flight_plans` VALUES (38, 130, '2026-01-04', 'J09', '8R', '{\"captain\": \"郑浩\", \"copilot\": \"李静\", \"attendants\": [\"王娜\", \"刘涛\"], \"crew_count\": 4}', '准点', '2025-12-26 18:19:40', '2026-01-04 16:30:44');
INSERT INTO `flight_plans` VALUES (39, 131, '2026-01-04', 'H01', '27C', '{\"captain\": \"王明\", \"copilot\": \"赵芳\", \"attendants\": [\"赵娜\", \"刘浩\"], \"crew_count\": 5}', '准点', '2025-12-26 18:19:40', '2026-01-04 16:25:16');
INSERT INTO `flight_plans` VALUES (40, 132, '2026-01-04', 'B20', '13L', '{\"captain\": \"周杰\", \"copilot\": \"郑敏\", \"attendants\": [\"周婷\", \"张明\"], \"crew_count\": 4}', '准点', '2026-01-04 18:19:40', '2026-01-04 16:25:04');
INSERT INTO `flight_plans` VALUES (41, 128, '2026-01-04', 'C12', '5L', '{\"captain\": \"吴涛\", \"copilot\": \"王芳\", \"attendants\": [\"陈婷\", \"王浩\"], \"crew_count\": 5}', '延误', '2026-01-04 18:19:40', '2026-01-04 15:51:29');
INSERT INTO `flight_plans` VALUES (42, 133, '2026-01-04', 'H11', '17L', '{\"captain\": \"郑浩\", \"copilot\": \"陈静\", \"attendants\": [\"张丽\", \"李浩\"], \"crew_count\": 4}', '准点', '2026-01-04 15:19:40', '2026-01-19 19:49:40');

-- ----------------------------
-- Table structure for flights
-- ----------------------------
DROP TABLE IF EXISTS `flights`;
CREATE TABLE `flights`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `aircraft_type_id` int NOT NULL,
  `origin_airport` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dest_airport` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sched_dep_time` datetime NOT NULL,
  `sched_arr_time` datetime NOT NULL,
  `status` enum('scheduled','delayed','cancelled','boarding','departed','arrived') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'scheduled',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '基础票价（单位：元）',
  `route_info` json NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flight_no_time`(`flight_no` ASC, `sched_dep_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `fk_flights_aircraft`(`aircraft_type_id` ASC) USING BTREE,
  CONSTRAINT `fk_flights_aircraft` FOREIGN KEY (`aircraft_type_id`) REFERENCES `aircraft_types` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1507 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '航班信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of flights
-- ----------------------------
INSERT INTO `flights` VALUES (11, 'CA1519', 6, 'PEK', 'SHA', '2026-01-07 09:30:00', '2026-01-07 11:55:00', 'scheduled', 2150.00, '[]', '2025-12-28 19:49:25', '2026-01-03 11:29:27');
INSERT INTO `flights` VALUES (12, 'CZ8885', 7, 'PKX', 'SHA', '2026-01-07 13:00:00', '2026-01-07 14:55:00', 'scheduled', 451.00, '[]', '2025-12-05 19:49:25', '2026-01-03 11:27:20');
INSERT INTO `flights` VALUES (13, 'HO7610(CZ8889)', 8, 'PKX', 'PVG', '2026-01-07 20:45:00', '2026-01-07 23:10:00', 'scheduled', 1590.00, '[]', '2025-12-05 19:49:25', '2026-01-03 10:59:08');
INSERT INTO `flights` VALUES (14, 'CA1501', 9, 'PEK', 'SHA', '2026-01-07 08:30:00', '2026-01-07 10:55:00', 'scheduled', 2150.00, '[]', '2025-12-05 19:49:25', '2026-01-03 11:30:21');
INSERT INTO `flights` VALUES (15, 'HO5417(MU5158)', 10, 'PEK', 'PVG', '2026-01-07 14:30:00', '2026-01-07 17:00:00', 'scheduled', 1020.00, '[]', '2025-12-05 19:49:25', '2026-01-03 11:03:50');
INSERT INTO `flights` VALUES (104, 'CA1202', 6, '北京首都国际机场', '上海虹桥国际机场', '2025-12-10 08:00:00', '2025-12-10 12:00:00', 'scheduled', 1200.00, '[]', '2025-12-05 19:49:25', '2026-01-03 10:47:22');
INSERT INTO `flights` VALUES (105, 'MU5301', 7, '上海浦东国际机场', '广州白云国际机场', '2025-12-09 09:00:00', '2025-12-09 12:00:00', 'scheduled', 950.00, '[]', '2025-12-05 19:49:25', '2026-01-03 10:47:22');
INSERT INTO `flights` VALUES (106, 'CZ3401', 8, '广州白云国际机场', '成都双流国际机场', '2025-12-09 11:00:00', '2025-12-09 13:30:00', 'scheduled', 850.00, '{\"航路\": \"直飞\"}', '2025-12-05 19:49:25', '2025-12-05 19:49:25');
INSERT INTO `flights` VALUES (107, 'HU7701', 9, '深圳宝安国际机场', '北京首都国际机场', '2025-12-09 13:00:00', '2025-12-09 16:30:00', 'scheduled', 1350.00, '{\"航路\": \"直飞\"}', '2025-12-05 19:49:25', '2025-12-05 19:49:25');
INSERT INTO `flights` VALUES (108, 'CA1301', 6, '北京首都国际机场', '深圳宝安国际机场', '2025-12-09 15:00:00', '2025-12-09 19:00:00', 'scheduled', 1400.00, '{\"航路\": \"经停武汉\"}', '2025-12-05 19:49:25', '2025-12-05 19:49:25');
INSERT INTO `flights` VALUES (109, 'MU5111', 7, '上海虹桥国际机场', '北京首都国际机场', '2025-12-09 16:00:00', '2025-12-09 18:30:00', 'scheduled', 1100.00, '{\"航路\": \"常规\"}', '2025-12-05 19:49:25', '2025-12-05 19:49:25');
INSERT INTO `flights` VALUES (110, '9C8811', 10, '上海虹桥国际机场', '厦门高崎国际机场', '2025-12-10 10:00:00', '2025-12-10 12:00:00', 'scheduled', 650.00, '{\"航路\": \"直飞\"}', '2025-12-05 19:49:25', '2025-12-05 19:49:25');
INSERT INTO `flights` VALUES (111, '3U8511', 6, '成都天府国际机场', '重庆江北国际机场', '2025-12-10 12:00:00', '2025-12-10 13:00:00', 'scheduled', 450.00, '{\"航路\": \"直飞\"}', '2025-12-05 19:49:25', '2025-12-05 19:49:25');
INSERT INTO `flights` VALUES (112, 'HO1235', 9, '杭州萧山国际机场', '西安咸阳国际机场', '2025-12-10 14:00:00', '2025-12-10 16:30:00', 'scheduled', 780.00, '{\"航路\": \"直飞\"}', '2025-12-05 19:49:25', '2025-12-05 19:49:25');
INSERT INTO `flights` VALUES (113, 'MF8101', 8, '福州长乐国际机场', '南京禄口国际机场', '2025-12-10 16:00:00', '2025-12-10 17:30:00', 'scheduled', 680.00, '{\"航路\": \"直飞\"}', '2025-12-05 19:49:25', '2025-12-05 19:49:25');
INSERT INTO `flights` VALUES (124, 'CA1202', 6, '北京首都国际机场', '上海虹桥国际机场', '2025-12-11 08:00:00', '2025-12-11 12:00:00', 'scheduled', 1200.00, '{\"航路\": \"常规\"}', '2025-12-26 17:26:33', '2025-12-26 17:26:33');
INSERT INTO `flights` VALUES (125, 'MU5435', 7, '上海浦东国际机场', '成都双流国际机场', '2025-12-11 10:00:00', '2025-12-11 14:00:00', 'scheduled', 850.00, '{\"航路\": \"经停武汉天河国际机场\"}', '2025-12-26 17:26:33', '2025-12-26 17:26:33');
INSERT INTO `flights` VALUES (126, 'CZ3102', 8, '广州白云国际机场', '北京首都国际机场', '2025-12-11 12:00:00', '2025-12-11 16:00:00', 'scheduled', 900.00, '{\"航路\": \"直飞\"}', '2025-12-26 17:26:33', '2025-12-26 17:26:33');
INSERT INTO `flights` VALUES (127, 'HU7616', 9, '北京首都国际机场', '上海虹桥国际机场', '2025-12-25 14:00:00', '2025-12-26 18:00:00', 'scheduled', 950.00, '{\"航路\": \"直飞\"}', '2025-12-26 17:26:33', '2025-12-26 17:29:59');
INSERT INTO `flights` VALUES (128, 'HO1235', 10, '上海虹桥国际机场', '成都天府国际机场', '2025-12-26 16:00:00', '2025-12-26 20:00:00', 'scheduled', 1000.00, '{\"航路\": \"直飞\"}', '2025-12-26 17:26:33', '2025-12-26 17:29:59');
INSERT INTO `flights` VALUES (129, 'CA1301', 6, '北京首都国际机场', '深圳宝安国际机场', '2025-12-26 08:00:00', '2025-12-26 12:00:00', 'scheduled', 1500.00, '{\"航路\": \"直飞\"}', '2025-12-26 17:26:33', '2025-12-26 17:29:59');
INSERT INTO `flights` VALUES (130, 'MU5301', 7, '上海虹桥国际机场', '广州白云国际机场', '2025-12-26 10:00:00', '2025-12-26 13:00:00', 'scheduled', 950.00, '{\"航路\": \"直飞\"}', '2025-12-26 17:26:33', '2025-12-26 17:29:59');
INSERT INTO `flights` VALUES (131, 'CZ3401', 8, '广州白云国际机场', '成都双流国际机场', '2025-12-26 12:00:00', '2025-12-26 14:30:00', 'scheduled', 850.00, '{\"航路\": \"直飞\"}', '2025-12-26 17:26:33', '2025-12-26 17:29:59');
INSERT INTO `flights` VALUES (132, 'HU7701', 9, '深圳宝安国际机场', '北京首都国际机场', '2025-12-26 14:00:00', '2025-12-26 17:30:00', 'scheduled', 1350.00, '{\"航路\": \"直飞\"}', '2025-12-26 17:26:33', '2025-12-26 17:29:15');
INSERT INTO `flights` VALUES (133, '9C8811', 10, '上海虹桥国际机场', '厦门高崎国际机场', '2026-01-04 17:30:59', '2026-01-04 22:00:00', 'scheduled', 650.00, '{\"航路\": \"直飞\"}', '2025-12-26 17:26:33', '2026-01-04 15:52:45');
INSERT INTO `flights` VALUES (1001, 'TEST1001', 1, 'PEK', 'PVG', '2025-12-26 08:00:00', '2025-12-26 10:00:00', 'departed', 680.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:37:52', '2025-12-26 19:38:29');
INSERT INTO `flights` VALUES (1002, 'TEST1002', 1, 'CAN', 'SZX', '2025-12-26 09:00:00', '2025-12-26 10:10:00', 'delayed', 420.00, '{\"route\": \"CAN→SZX\"}', '2025-12-26 19:37:52', '2025-12-26 19:38:29');
INSERT INTO `flights` VALUES (1003, 'TEST1003', 1, 'BJS', 'SHA', '2025-12-26 11:00:00', '2025-12-26 13:00:00', 'cancelled', 520.00, '{\"route\": \"BJS→SHA\"}', '2025-12-26 19:37:52', '2025-12-26 19:38:29');
INSERT INTO `flights` VALUES (1004, 'TEST1004', 1, 'PEK', 'SZX', '2025-12-26 12:00:00', '2025-12-26 14:00:00', 'scheduled', 720.00, '{\"route\": \"PEK→SZX\"}', '2025-12-26 19:37:52', '2025-12-26 19:38:29');
INSERT INTO `flights` VALUES (1005, 'TEST1005', 1, 'SHA', 'CAN', '2025-12-26 13:00:00', '2025-12-26 15:00:00', 'boarding', 650.00, '{\"route\": \"SHA→CAN\"}', '2025-12-26 19:37:52', '2025-12-26 19:38:29');
INSERT INTO `flights` VALUES (1101, 'TREND1101', 1, 'PEK', 'PVG', '2025-12-20 08:00:00', '2025-12-20 10:00:00', 'departed', 600.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1102, 'TREND1102', 1, 'PEK', 'PVG', '2025-12-20 09:00:00', '2025-12-20 11:00:00', 'departed', 600.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1103, 'TREND1103', 1, 'PEK', 'PVG', '2025-12-20 10:00:00', '2025-12-20 12:00:00', 'delayed', 600.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1104, 'TREND1104', 1, 'PEK', 'PVG', '2025-12-21 08:00:00', '2025-12-21 10:00:00', 'departed', 600.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1105, 'TREND1105', 1, 'PEK', 'PVG', '2025-12-21 09:00:00', '2025-12-21 11:00:00', 'delayed', 600.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1106, 'TREND1106', 1, 'PEK', 'PVG', '2025-12-21 10:00:00', '2025-12-21 12:00:00', 'cancelled', 600.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1107, 'TREND1107', 1, 'PEK', 'SZX', '2025-12-22 08:00:00', '2025-12-22 10:00:00', 'departed', 580.00, '{\"route\": \"PEK→SZX\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1108, 'TREND1108', 1, 'PEK', 'SZX', '2025-12-22 09:00:00', '2025-12-22 11:00:00', 'departed', 580.00, '{\"route\": \"PEK→SZX\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1109, 'TREND1109', 1, 'PEK', 'SZX', '2025-12-22 10:00:00', '2025-12-22 12:00:00', 'delayed', 580.00, '{\"route\": \"PEK→SZX\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1110, 'TREND1110', 1, 'SHA', 'CAN', '2025-12-23 08:00:00', '2025-12-23 10:00:00', 'departed', 650.00, '{\"route\": \"SHA→CAN\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1111, 'TREND1111', 1, 'SHA', 'CAN', '2025-12-23 09:00:00', '2025-12-23 11:00:00', 'delayed', 650.00, '{\"route\": \"SHA→CAN\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1112, 'TREND1112', 1, 'SHA', 'CAN', '2025-12-23 10:00:00', '2025-12-23 12:00:00', 'departed', 650.00, '{\"route\": \"SHA→CAN\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1113, 'TREND1113', 1, 'CAN', 'SZX', '2025-12-24 08:00:00', '2025-12-24 10:00:00', 'cancelled', 420.00, '{\"route\": \"CAN→SZX\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1114, 'TREND1114', 1, 'CAN', 'SZX', '2025-12-24 09:00:00', '2025-12-24 11:00:00', 'departed', 420.00, '{\"route\": \"CAN→SZX\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1115, 'TREND1115', 1, 'CAN', 'SZX', '2025-12-24 10:00:00', '2025-12-24 12:00:00', 'departed', 420.00, '{\"route\": \"CAN→SZX\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1116, 'TREND1116', 1, 'BJS', 'SHA', '2025-12-25 08:00:00', '2025-12-25 10:00:00', 'departed', 520.00, '{\"route\": \"BJS→SHA\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1117, 'TREND1117', 1, 'BJS', 'SHA', '2025-12-25 09:00:00', '2025-12-25 11:00:00', 'delayed', 520.00, '{\"route\": \"BJS→SHA\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1118, 'TREND1118', 1, 'BJS', 'SHA', '2025-12-25 10:00:00', '2025-12-25 12:00:00', 'departed', 520.00, '{\"route\": \"BJS→SHA\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1119, 'TREND1119', 1, 'PEK', 'PVG', '2025-12-26 06:00:00', '2025-12-26 08:00:00', 'departed', 680.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1120, 'TREND1120', 1, 'PEK', 'PVG', '2025-12-26 07:00:00', '2025-12-26 09:00:00', 'delayed', 680.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1121, 'TREND1121', 1, 'PEK', 'PVG', '2025-12-26 11:00:00', '2025-12-26 13:00:00', 'departed', 680.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1122, 'TREND1122', 1, 'PEK', 'SZX', '2025-12-26 14:00:00', '2025-12-26 16:00:00', 'scheduled', 720.00, '{\"route\": \"PEK→SZX\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1123, 'TREND1123', 1, 'SHA', 'CAN', '2025-12-26 15:00:00', '2025-12-26 17:00:00', 'boarding', 650.00, '{\"route\": \"SHA→CAN\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1124, 'TREND1124', 1, 'SHA', 'CAN', '2025-12-26 16:00:00', '2025-12-26 18:00:00', 'delayed', 650.00, '{\"route\": \"SHA→CAN\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1125, 'TREND1125', 1, 'CAN', 'SZX', '2025-12-26 17:00:00', '2025-12-26 19:00:00', 'departed', 420.00, '{\"route\": \"CAN→SZX\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1126, 'TREND1126', 1, 'BJS', 'SHA', '2025-12-26 18:00:00', '2025-12-26 20:00:00', 'departed', 520.00, '{\"route\": \"BJS→SHA\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1127, 'TREND1127', 1, 'BJS', 'SHA', '2025-12-26 19:00:00', '2025-12-26 21:00:00', 'cancelled', 520.00, '{\"route\": \"BJS→SHA\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1128, 'TREND1128', 1, 'PEK', 'PVG', '2025-12-27 08:00:00', '2025-12-27 10:00:00', 'departed', 680.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1129, 'TREND1129', 1, 'PEK', 'PVG', '2025-12-27 09:00:00', '2025-12-27 11:00:00', 'delayed', 680.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1130, 'TREND1130', 1, 'PEK', 'PVG', '2025-12-27 10:00:00', '2025-12-27 12:00:00', 'departed', 680.00, '{\"route\": \"PEK→PVG\"}', '2025-12-26 19:41:49', '2025-12-26 19:41:53');
INSERT INTO `flights` VALUES (1131, 'INTL1001', 23, 'LHR', 'PEK', '2025-12-30 09:00:00', '2025-12-30 23:30:00', 'scheduled', 4200.00, '{\"notes\": \"LHR→PEK\"}', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `flights` VALUES (1132, 'INTL2002', 23, 'JFK', 'PVG', '2025-12-31 10:00:00', '2026-01-01 02:30:00', 'scheduled', 4800.00, '{\"notes\": \"JFK→PVG\"}', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `flights` VALUES (1133, 'INTL3003', 23, 'NRT', 'CAN', '2025-12-29 13:00:00', '2025-12-29 18:30:00', 'scheduled', 3200.00, '{\"notes\": \"NRT→CAN\"}', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `flights` VALUES (1134, 'INTL4004', 23, 'SYD', 'SHA', '2025-12-28 08:00:00', '2025-12-28 20:30:00', 'scheduled', 5500.00, '{\"notes\": \"SYD→SHA\"}', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `flights` VALUES (1135, 'INTL5005', 23, 'DXB', 'SZX', '2026-01-04 02:00:00', '2026-01-05 14:30:00', 'scheduled', 3900.00, '{\"notes\": \"DXB→SZX\"}', '2025-12-26 22:12:10', '2026-01-04 15:45:04');
INSERT INTO `flights` VALUES (1505, 'CNM911', 9, '成都天府国际机场', '日本机场', '2026-01-07 16:03:00', '2026-01-08 16:03:00', 'scheduled', 800.00, '{\"distance_km\": 1200, \"meal_service\": true}', '2026-01-04 16:03:28', '2026-01-04 16:03:28');
INSERT INTO `flights` VALUES (1506, 'AMR900', 9, '成都天府国际机场', '东京机场', '2026-01-07 16:04:00', '2026-01-08 16:04:00', 'scheduled', 850.00, '{\"distance_km\": 1200, \"meal_service\": true}', '2026-01-04 16:04:25', '2026-01-04 16:04:25');

-- ----------------------------
-- Table structure for frequent_passengers
-- ----------------------------
DROP TABLE IF EXISTS `frequent_passengers`;
CREATE TABLE `frequent_passengers`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int NOT NULL COMMENT '所属用户ID（关联users表，实现数据隔离）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '乘客姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证号（18位）',
  `relationship` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '与用户的关系（配偶、子女、父母、兄弟姐妹、朋友、同事、其他）',
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注信息',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否设为默认（0-否，1-是）',
  `status` enum('active','deleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态（active-有效，deleted-已删除）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_card`(`user_id` ASC, `id_card` ASC) USING BTREE COMMENT '同一用户下身份证号唯一',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引（用于快速查询该用户的常用乘客）',
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE COMMENT '用户ID和状态联合索引',
  INDEX `idx_id_card`(`id_card` ASC) USING BTREE COMMENT '身份证号索引（用于验证唯一性）',
  CONSTRAINT `fk_frequent_passenger_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '常用乘客信息表（每个用户的数据完全隔离）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of frequent_passengers
-- ----------------------------
INSERT INTO `frequent_passengers` VALUES (1, 3, '李静', '110101199102028644', '配偶', '13900139000', '妻子，喜欢靠窗座位', 0, 'active', '2025-12-06 11:18:50', '2025-12-06 11:18:50');
INSERT INTO `frequent_passengers` VALUES (2, 3, '张浩然', '110101201512036215', '子女', '15973773354', '儿子，儿童票', 0, 'active', '2025-12-06 11:18:50', '2025-12-06 14:33:56');
INSERT INTO `frequent_passengers` VALUES (3, 3, '张建', '110101196505128976', '父母', '13600136000', '父亲，提醒准备靠过道座位', 0, 'active', '2025-12-06 11:18:50', '2025-12-18 22:05:15');
INSERT INTO `frequent_passengers` VALUES (4, 3, '王丽美', '110101198803157894', '同事', '13500135000', '项目团队成员，常一同出差', 1, 'deleted', '2025-12-06 11:18:50', '2025-12-06 11:45:15');
INSERT INTO `frequent_passengers` VALUES (5, 3, '李四', '500100194211239452', '子女', '14738045152', '儿童票', 0, 'deleted', '2025-12-06 11:36:13', '2025-12-06 11:38:27');
INSERT INTO `frequent_passengers` VALUES (6, 3, '李梅1', '123456789123456789', '子女', '15973773354', '同事', 0, 'deleted', '2025-12-06 11:46:07', '2025-12-06 20:37:28');
INSERT INTO `frequent_passengers` VALUES (7, 3, '999', '123456789123456745', '子女', '15973773357', '儿童票', 0, 'deleted', '2025-12-06 20:42:46', '2025-12-07 10:02:03');
INSERT INTO `frequent_passengers` VALUES (8, 3, '小红', '123456789987654321', '子女', '15973773359', '儿童票', 0, 'deleted', '2025-12-07 10:02:45', '2025-12-07 11:44:02');
INSERT INTO `frequent_passengers` VALUES (9, 3, '小明', '123456789998765432', '子女', '15973773355', '儿童票', 0, 'deleted', '2025-12-07 11:44:36', '2025-12-07 12:29:14');
INSERT INTO `frequent_passengers` VALUES (10, 3, '小红呀', '123456789876543219', '子女', '1543973773355', '儿童', 0, 'deleted', '2025-12-07 12:30:19', '2025-12-07 08:53:54');
INSERT INTO `frequent_passengers` VALUES (11, 3, '李好', '123456789997654323', '子女', '15973773354', '儿童票呀', 0, 'deleted', '2025-12-07 08:54:46', '2025-12-09 01:30:04');
INSERT INTO `frequent_passengers` VALUES (12, 3, '李浩', '123456789987654322', '子女', '15973773353', '儿童票', 0, 'deleted', '2025-12-09 01:30:52', '2025-12-09 10:21:58');
INSERT INTO `frequent_passengers` VALUES (13, 3, 'yy', '123456789987654333', '父母', '15973773354', '靠', 0, 'deleted', '2025-12-09 10:22:47', '2025-12-09 03:59:21');
INSERT INTO `frequent_passengers` VALUES (14, 3, '667', '123456789987665433', '父母', '15973777352', '2222', 0, 'deleted', '2025-12-09 04:00:27', '2025-12-09 08:19:20');
INSERT INTO `frequent_passengers` VALUES (15, 3, '李四呀呀', '123456789987654231', '子女', '15973773354', '儿童票', 0, 'deleted', '2025-12-09 08:20:18', '2025-12-11 00:10:16');
INSERT INTO `frequent_passengers` VALUES (16, 3, '力哈', '123456789987654329', '子女', '15973773356', '1111111', 0, 'deleted', '2025-12-11 00:10:50', '2025-12-18 22:16:09');
INSERT INTO `frequent_passengers` VALUES (17, 3, '李好呀呀', '123456789987654344', '子女', '15973773354', '122334', 0, 'deleted', '2025-12-18 22:16:41', '2025-12-24 10:31:33');
INSERT INTO `frequent_passengers` VALUES (18, 3, '周瑾1', '123456789987654378', '子女', '15973773390', '儿童票', 0, 'deleted', '2025-12-24 10:32:31', '2025-12-25 17:49:32');
INSERT INTO `frequent_passengers` VALUES (19, 3, '李好呀7', '123456789987654376', '子女', '15973773359', '儿童票', 0, 'deleted', '2025-12-25 17:51:25', '2025-12-28 20:12:42');
INSERT INTO `frequent_passengers` VALUES (20, 3, '你好呀', '123456789987654843', '子女', '15973773359', '122333', 0, 'active', '2025-12-28 20:14:48', '2025-12-28 20:14:48');

-- ----------------------------
-- Table structure for loyalty_points
-- ----------------------------
DROP TABLE IF EXISTS `loyalty_points`;
CREATE TABLE `loyalty_points`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `passenger_id` int NOT NULL,
  `points` int NOT NULL,
  `change_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ref_order_id` bigint NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_passenger_time`(`passenger_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `fk_lp_order`(`ref_order_id` ASC) USING BTREE,
  CONSTRAINT `fk_lp_order` FOREIGN KEY (`ref_order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_lp_user` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 122 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分变动（乘客隔离）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of loyalty_points
-- ----------------------------
INSERT INTO `loyalty_points` VALUES (1, 3, 500, 'EARN', 4, '生日礼券兑换500积分', '2025-10-01 09:30:00');
INSERT INTO `loyalty_points` VALUES (2, 3, 300, 'EARN', 4, '新用户注册券兑换300积分', '2025-10-02 14:15:00');
INSERT INTO `loyalty_points` VALUES (3, 3, 800, 'EARN', 5, '国庆活动满减券兑换800积分', '2025-10-03 11:20:00');
INSERT INTO `loyalty_points` VALUES (5, 3, 10000, 'EARN', 5, '会员升级券兑换1000积分', '2025-10-05 10:00:00');
INSERT INTO `loyalty_points` VALUES (29, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-19 20:27:06');
INSERT INTO `loyalty_points` VALUES (31, 3, 1000, 'EARN', 21, '购票获得积分：1000分', '2025-12-20 16:32:44');
INSERT INTO `loyalty_points` VALUES (33, 3, 1000, 'EARN', 23, '购票获得积分：1000分', '2025-12-20 18:45:57');
INSERT INTO `loyalty_points` VALUES (34, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-20 18:47:21');
INSERT INTO `loyalty_points` VALUES (35, 3, 950, 'EARN', 24, '购票获得积分：950分', '2025-12-20 18:47:31');
INSERT INTO `loyalty_points` VALUES (36, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-20 18:48:39');
INSERT INTO `loyalty_points` VALUES (37, 3, 750, 'EARN', 25, '购票获得积分：750分', '2025-12-20 18:48:46');
INSERT INTO `loyalty_points` VALUES (38, 3, 950, 'EARN', 26, '购票获得积分：950分', '2025-12-20 18:50:44');
INSERT INTO `loyalty_points` VALUES (39, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-20 18:57:34');
INSERT INTO `loyalty_points` VALUES (40, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-20 18:57:51');
INSERT INTO `loyalty_points` VALUES (41, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-20 18:58:56');
INSERT INTO `loyalty_points` VALUES (42, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-20 19:02:40');
INSERT INTO `loyalty_points` VALUES (43, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-20 19:04:07');
INSERT INTO `loyalty_points` VALUES (44, 3, 950, 'EARN', 27, '购票获得积分：950分', '2025-12-20 19:04:10');
INSERT INTO `loyalty_points` VALUES (45, 3, 800, 'EARN', 28, '购票获得积分：800分', '2025-12-20 20:54:07');
INSERT INTO `loyalty_points` VALUES (46, 3, 800, 'EARN', 29, '购票获得积分：800分', '2025-12-20 20:59:14');
INSERT INTO `loyalty_points` VALUES (47, 3, 800, 'EARN', 30, '购票获得积分：800分', '2025-12-20 21:00:35');
INSERT INTO `loyalty_points` VALUES (48, 3, 1, 'EARN', 31, '购票获得积分：1分', '2025-12-20 21:35:19');
INSERT INTO `loyalty_points` VALUES (49, 3, 1, 'EARN', 32, '购票获得积分：1分', '2025-12-20 21:59:20');
INSERT INTO `loyalty_points` VALUES (50, 3, 1, 'EARN', 33, '购票获得积分：1分', '2025-12-20 22:34:41');
INSERT INTO `loyalty_points` VALUES (51, 3, 1, 'EARN', 34, '购票获得积分：1分', '2025-12-20 22:41:01');
INSERT INTO `loyalty_points` VALUES (52, 3, 1, 'EARN', 35, '购票获得积分：1分', '2025-12-20 22:48:37');
INSERT INTO `loyalty_points` VALUES (53, 3, 1, 'EARN', 36, '购票获得积分：1分', '2025-12-20 22:57:07');
INSERT INTO `loyalty_points` VALUES (54, 3, 1, 'EARN', 37, '购票获得积分：1分', '2025-12-20 22:58:58');
INSERT INTO `loyalty_points` VALUES (55, 3, 1, 'EARN', 38, '购票获得积分：1分', '2025-12-20 23:02:12');
INSERT INTO `loyalty_points` VALUES (56, 3, 1, 'EARN', 39, '购票获得积分：1分', '2025-12-20 23:06:48');
INSERT INTO `loyalty_points` VALUES (57, 3, 1, 'EARN', 40, '购票获得积分：1分', '2025-12-20 23:09:43');
INSERT INTO `loyalty_points` VALUES (58, 3, 1, 'EARN', 41, '购票获得积分：1分', '2025-12-20 23:14:36');
INSERT INTO `loyalty_points` VALUES (59, 3, 1, 'EARN', 42, '购票获得积分：1分', '2025-12-20 23:15:05');
INSERT INTO `loyalty_points` VALUES (60, 3, 1, 'EARN', 43, '购票获得积分：1分', '2025-12-20 23:16:22');
INSERT INTO `loyalty_points` VALUES (61, 3, 1, 'EARN', 44, '购票获得积分：1分', '2025-12-20 23:17:09');
INSERT INTO `loyalty_points` VALUES (63, 3, 1, 'EARN', 46, '购票获得积分：1分', '2025-12-21 10:24:07');
INSERT INTO `loyalty_points` VALUES (64, 3, 1, 'EARN', 47, '购票获得积分：1分', '2025-12-21 10:24:28');
INSERT INTO `loyalty_points` VALUES (65, 3, 1, 'EARN', 48, '购票获得积分：1分', '2025-12-21 10:25:37');
INSERT INTO `loyalty_points` VALUES (66, 3, 1, 'EARN', 49, '购票获得积分：1分', '2025-12-21 10:26:59');
INSERT INTO `loyalty_points` VALUES (67, 3, 1, 'EARN', 50, '购票获得积分：1分', '2025-12-21 10:28:29');
INSERT INTO `loyalty_points` VALUES (69, 3, 1, 'EARN', 52, '购票获得积分：1分', '2025-12-21 10:35:06');
INSERT INTO `loyalty_points` VALUES (72, 3, 850, 'EARN', 55, '购票获得积分：850分', '2025-12-21 11:11:41');
INSERT INTO `loyalty_points` VALUES (73, 3, 1, 'EARN', 56, '购票获得积分：1分', '2025-12-21 11:23:47');
INSERT INTO `loyalty_points` VALUES (74, 3, 1, 'EARN', 57, '购票获得积分：1分', '2025-12-21 11:29:20');
INSERT INTO `loyalty_points` VALUES (75, 3, 1, 'EARN', 58, '购票获得积分：1分', '2025-12-21 11:31:56');
INSERT INTO `loyalty_points` VALUES (76, 3, 1, 'EARN', 59, '购票获得积分：1分', '2025-12-21 13:37:18');
INSERT INTO `loyalty_points` VALUES (77, 3, 201, 'EARN', 60, '购票获得积分：201分', '2025-12-21 14:06:23');
INSERT INTO `loyalty_points` VALUES (78, 3, 201, 'EARN', 61, '购票获得积分：201分', '2025-12-21 17:00:46');
INSERT INTO `loyalty_points` VALUES (79, 3, 201, 'EARN', 62, '购票获得积分：201分', '2025-12-21 21:30:12');
INSERT INTO `loyalty_points` VALUES (80, 3, 201, 'EARN', 63, '购票获得积分：201分', '2025-12-21 21:33:00');
INSERT INTO `loyalty_points` VALUES (81, 3, 1, 'EARN', 64, '购票获得积分：1分', '2025-12-22 11:23:47');
INSERT INTO `loyalty_points` VALUES (82, 3, 201, 'EARN', 65, '购票获得积分：201分', '2025-12-22 17:52:03');
INSERT INTO `loyalty_points` VALUES (83, 3, 1, 'EARN', 66, '购票获得积分：1分', '2025-12-22 19:30:19');
INSERT INTO `loyalty_points` VALUES (84, 3, 1, 'EARN', 67, '购票获得积分：1分', '2025-12-22 19:48:18');
INSERT INTO `loyalty_points` VALUES (85, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-22 19:51:08');
INSERT INTO `loyalty_points` VALUES (86, 3, 151, 'EARN', 68, '购票获得积分：151分', '2025-12-22 19:51:52');
INSERT INTO `loyalty_points` VALUES (87, 3, 201, 'EARN', 69, '购票获得积分：201分', '2025-12-23 10:49:25');
INSERT INTO `loyalty_points` VALUES (88, 3, 201, 'EARN', 70, '购票获得积分：201分', '2025-12-23 16:07:15');
INSERT INTO `loyalty_points` VALUES (89, 3, 201, 'EARN', 71, '购票获得积分：201分', '2025-12-23 16:19:35');
INSERT INTO `loyalty_points` VALUES (90, 3, 201, 'EARN', 72, '购票获得积分：201分', '2025-12-23 16:20:32');
INSERT INTO `loyalty_points` VALUES (91, 3, 201, 'EARN', 73, '购票获得积分：201分', '2025-12-23 16:58:16');
INSERT INTO `loyalty_points` VALUES (92, 3, 201, 'EARN', 74, '购票获得积分：201分', '2025-12-23 17:11:21');
INSERT INTO `loyalty_points` VALUES (93, 3, 201, 'EARN', 75, '购票获得积分：201分', '2025-12-23 17:39:44');
INSERT INTO `loyalty_points` VALUES (94, 3, 201, 'EARN', 76, '购票获得积分：201分', '2025-12-23 17:59:16');
INSERT INTO `loyalty_points` VALUES (95, 3, 201, 'EARN', 77, '购票获得积分：201分', '2025-12-23 18:00:38');
INSERT INTO `loyalty_points` VALUES (96, 3, 201, 'EARN', 81, '购票获得积分：201分', '2025-12-23 21:00:32');
INSERT INTO `loyalty_points` VALUES (97, 3, 201, 'EARN', 83, '购票获得积分：201分', '2025-12-23 21:16:40');
INSERT INTO `loyalty_points` VALUES (98, 3, 201, 'EARN', 84, '购票获得积分：201分', '2025-12-23 21:19:38');
INSERT INTO `loyalty_points` VALUES (99, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-24 10:33:35');
INSERT INTO `loyalty_points` VALUES (100, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-24 10:34:09');
INSERT INTO `loyalty_points` VALUES (101, 3, 1003, 'EARN', 87, '购票获得积分：1003分', '2025-12-24 10:38:21');
INSERT INTO `loyalty_points` VALUES (102, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-24 12:51:30');
INSERT INTO `loyalty_points` VALUES (103, 3, 146, 'EARN', 89, '购票获得积分：146分', '2025-12-24 13:01:58');
INSERT INTO `loyalty_points` VALUES (104, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-24 13:09:20');
INSERT INTO `loyalty_points` VALUES (105, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-24 13:10:16');
INSERT INTO `loyalty_points` VALUES (106, 3, 151, 'EARN', 90, '购票获得积分：151分', '2025-12-24 13:12:03');
INSERT INTO `loyalty_points` VALUES (107, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-24 13:13:16');
INSERT INTO `loyalty_points` VALUES (108, 3, 1000, 'SPEND', NULL, '积分兑换：¥50 代金券', '2025-12-24 13:15:20');
INSERT INTO `loyalty_points` VALUES (109, 3, 151, 'EARN', 91, '购票获得积分：151分', '2025-12-24 13:16:11');
INSERT INTO `loyalty_points` VALUES (110, 3, 1000, 'SPEND', 92, '积分兑换：¥50 代金券', '2025-12-24 13:22:25');
INSERT INTO `loyalty_points` VALUES (111, 3, 151, 'EARN', 92, '购票获得积分：151分', '2025-12-24 13:25:02');
INSERT INTO `loyalty_points` VALUES (112, 3, 1000, 'SPEND', 93, '积分兑换：¥50 代金券', '2025-12-24 13:26:00');
INSERT INTO `loyalty_points` VALUES (113, 3, 151, 'EARN', 93, '购票获得积分：151分', '2025-12-24 13:26:42');
INSERT INTO `loyalty_points` VALUES (114, 3, 1000, 'SPEND', 97, '积分兑换：¥50 代金券', '2025-12-25 17:52:16');
INSERT INTO `loyalty_points` VALUES (115, 3, 151, 'EARN', 97, '购票获得积分：151分', '2025-12-25 17:53:02');
INSERT INTO `loyalty_points` VALUES (116, 3, 1220, 'EARN', 98, '购票获得积分：1220分', '2025-12-25 18:02:34');
INSERT INTO `loyalty_points` VALUES (117, 3, 201, 'EARN', 107, '购票获得积分：201分', '2025-12-27 22:35:12');
INSERT INTO `loyalty_points` VALUES (118, 3, 1000, 'SPEND', 109, '积分兑换：¥50 代金券', '2025-12-28 20:20:15');
INSERT INTO `loyalty_points` VALUES (119, 3, 151, 'EARN', 109, '购票获得积分：151分', '2025-12-28 20:21:48');
INSERT INTO `loyalty_points` VALUES (120, 3, 2365, 'EARN', 113, '购票获得积分：2365分', '2026-01-03 11:37:05');
INSERT INTO `loyalty_points` VALUES (121, 3, 2365, 'EARN', 114, '购票获得积分：2365分', '2026-01-03 11:38:16');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `passenger_id` int NOT NULL COMMENT '乘客ID（关联users表，实现数据隔离）',
  `order_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `passenger_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名（冗余字段，便于查询，从users.real_name同步）',
  `route` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '航线（格式：出发地 → 目的地，如：北京 → 上海）',
  `flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '航班号',
  `ticket_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '机票号',
  `departure_time` datetime NULL DEFAULT NULL COMMENT '起飞时间',
  `arrival_time` datetime NULL DEFAULT NULL COMMENT '到达时间',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_passenger`(`passenger_id` ASC) USING BTREE,
  INDEX `idx_passenger_name`(`passenger_name` ASC) USING BTREE,
  INDEX `idx_route`(`route` ASC) USING BTREE,
  INDEX `idx_departure_time`(`departure_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 116 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单主表（乘客隔离，包含客户姓名、航线、起飞时间）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (4, 3, 'ORD202512081140003', '赵六', '上海浦东国际机场 → 东京成田国际机场', 'CA1234', '781-1234567890', '2025-11-09 19:30:00', '2025-11-09 21:30:00', 4800.00, '待出行', '2025-12-06 16:13:26', '2025-12-24 11:20:25');
INSERT INTO `orders` VALUES (5, 3, 'ORD202511281600008', '钱七', '西安咸阳国际机场 → 昆明长水国际机场', 'MU5678', '781-2345678901', '2025-12-10 14:30:00', '2025-12-10 17:00:00', 1000.00, '待出行', '2025-12-06 16:13:26', '2025-12-24 12:30:13');
INSERT INTO `orders` VALUES (21, 3, 'ORD202512201632440890', '张浩然', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220163244711841', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1000.00, '已完成', '2025-12-20 16:32:44', '2025-12-25 18:09:33');
INSERT INTO `orders` VALUES (23, 3, 'ORD202512201845579095', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220184557118324', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1000.00, 'deleted', '2025-12-20 18:45:57', '2025-12-25 18:08:50');
INSERT INTO `orders` VALUES (24, 3, 'ORD202512201847316460', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220184731107865', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 950.00, '待出行', '2025-12-20 18:47:31', '2025-12-20 18:47:31');
INSERT INTO `orders` VALUES (25, 3, 'ORD202512201848452096', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220184845757424', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 750.00, '待出行', '2025-12-20 18:48:46', '2025-12-20 18:48:46');
INSERT INTO `orders` VALUES (26, 3, 'ORD202512201850436585', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220185043056016', '2025-12-21 08:00:00', '2025-12-21 12:00:00', 950.00, '待出行', '2025-12-20 18:50:44', '2025-12-20 19:01:32');
INSERT INTO `orders` VALUES (27, 3, 'ORD202512201904097659', '张浩然', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220190409905491', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 950.00, '待出行', '2025-12-20 19:04:10', '2025-12-20 19:04:10');
INSERT INTO `orders` VALUES (28, 3, 'ORD202512202054064543', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220205406064693', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 800.00, '待出行', '2025-12-20 20:54:07', '2025-12-20 20:54:07');
INSERT INTO `orders` VALUES (29, 3, 'ORD202512202059140721', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220205914191442', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 800.00, '待出行', '2025-12-20 20:59:14', '2025-12-20 20:59:14');
INSERT INTO `orders` VALUES (30, 3, 'ORD202512202100358421', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220210035990606', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 800.00, '待出行', '2025-12-20 21:00:35', '2025-12-20 21:00:35');
INSERT INTO `orders` VALUES (31, 3, 'ORD202512202135182004', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220213518381726', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 21:35:18', '2025-12-20 21:35:19');
INSERT INTO `orders` VALUES (32, 3, 'ORD202512202159206482', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220215920915340', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 21:59:20', '2025-12-20 21:59:20');
INSERT INTO `orders` VALUES (33, 3, 'ORD202512202234402053', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220223440784657', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 22:34:41', '2025-12-20 22:34:41');
INSERT INTO `orders` VALUES (34, 3, 'ORD202512202241000219', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220224100982431', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 22:41:01', '2025-12-20 22:41:01');
INSERT INTO `orders` VALUES (35, 3, 'ORD202512202248372909', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220224837908096', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 22:48:37', '2025-12-20 22:48:37');
INSERT INTO `orders` VALUES (36, 3, 'ORD202512202257077856', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220225707345210', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 22:57:07', '2025-12-20 22:57:07');
INSERT INTO `orders` VALUES (37, 3, 'ORD202512202258572326', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220225857068278', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 22:58:58', '2025-12-20 22:58:58');
INSERT INTO `orders` VALUES (38, 3, 'ORD202512202302127976', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220230212515904', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 23:02:12', '2025-12-20 23:02:12');
INSERT INTO `orders` VALUES (39, 3, 'ORD202512202306480987', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220230648999521', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 23:06:48', '2025-12-20 23:06:48');
INSERT INTO `orders` VALUES (40, 3, 'ORD202512202309428473', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220230942706883', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 23:09:43', '2025-12-20 23:09:43');
INSERT INTO `orders` VALUES (41, 3, 'ORD202512202314366434', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220231436188945', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 23:14:36', '2025-12-20 23:14:36');
INSERT INTO `orders` VALUES (42, 3, 'ORD202512202315058025', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220231505341314', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 23:15:05', '2025-12-20 23:15:05');
INSERT INTO `orders` VALUES (43, 3, 'ORD202512202316218456', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220231621859189', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 23:16:22', '2025-12-20 23:16:22');
INSERT INTO `orders` VALUES (44, 3, 'ORD202512202317089477', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251220231708727305', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-20 23:17:09', '2025-12-20 23:17:09');
INSERT INTO `orders` VALUES (46, 3, 'ORD202512211024067914', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221102406359302', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-21 10:24:07', '2025-12-21 10:24:07');
INSERT INTO `orders` VALUES (47, 3, 'ORD202512211024288847', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221102428005051', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-21 10:24:28', '2025-12-21 10:24:28');
INSERT INTO `orders` VALUES (48, 3, 'ORD202512211025362820', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221102536633348', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待处理', '2025-12-21 10:25:36', '2025-12-23 20:42:50');
INSERT INTO `orders` VALUES (49, 3, 'ORD202512211026597704', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221102659687365', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待处理', '2025-12-21 10:26:59', '2025-12-23 20:30:17');
INSERT INTO `orders` VALUES (50, 3, 'ORD202512211028298071', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221102829796042', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待处理', '2025-12-21 10:28:29', '2025-12-23 20:08:34');
INSERT INTO `orders` VALUES (52, 3, 'ORD202512211035063881', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221103506631946', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待审核', '2025-12-21 10:35:06', '2025-12-23 19:47:38');
INSERT INTO `orders` VALUES (55, 3, 'ORD202512211111417363', '张建', '上海浦东国际机场 → 成都双流国际机场', 'MU5432', 'TKT20251221111141414645', '2025-12-09 10:00:00', '2025-12-09 14:00:00', 850.00, '待出行', '2025-12-21 11:11:41', '2026-01-04 15:10:41');
INSERT INTO `orders` VALUES (56, 3, 'ORD202512211123462144', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221112346334462', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待退款', '2025-12-21 11:23:47', '2026-01-04 15:10:17');
INSERT INTO `orders` VALUES (57, 3, 'ORD202512211129204244', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221112920743498', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待退款', '2025-12-21 11:29:20', '2026-01-04 15:10:29');
INSERT INTO `orders` VALUES (58, 3, 'ORD202512211131555077', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221113155865683', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待出行', '2025-12-21 11:31:56', '2025-12-25 11:53:34');
INSERT INTO `orders` VALUES (59, 3, 'ORD202512211337188162', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221133718100159', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待审核', '2025-12-21 13:37:18', '2025-12-23 11:33:58');
INSERT INTO `orders` VALUES (60, 3, 'ORD202512211406237817', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221140623352436', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '待审核', '2025-12-21 14:06:23', '2025-12-23 11:28:01');
INSERT INTO `orders` VALUES (61, 3, 'ORD202512211700461628', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221170046970917', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '待审核', '2025-12-21 17:00:46', '2025-12-23 11:21:08');
INSERT INTO `orders` VALUES (62, 3, 'ORD202512212130123567', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221213012392339', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '待审核', '2025-12-21 21:30:12', '2025-12-23 10:51:42');
INSERT INTO `orders` VALUES (63, 3, 'ORD202512212132594334', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251221213259488235', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '待出行', '2025-12-21 21:33:00', '2025-12-25 10:35:00');
INSERT INTO `orders` VALUES (64, 3, 'ORD202512221123477849', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251222112347297809', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 1.00, '待退款', '2025-12-22 11:23:47', '2025-12-24 22:25:19');
INSERT INTO `orders` VALUES (65, 3, 'ORD202512221752034043', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251222175203479338', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '待审核', '2025-12-22 17:52:03', '2025-12-23 10:45:51');
INSERT INTO `orders` VALUES (66, 3, 'ORD202512221930197043', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251222193019750131', '2025-12-31 08:00:00', '2025-12-31 12:00:00', 1.00, '待出行', '2025-12-22 19:30:19', '2025-12-30 10:44:36');
INSERT INTO `orders` VALUES (67, 3, 'ORD202512221948189133', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251222194818634023', '2025-12-31 08:00:00', '2025-12-31 12:00:00', 1.00, '待出行', '2025-12-22 19:48:18', '2025-12-30 10:44:36');
INSERT INTO `orders` VALUES (68, 3, 'ORD202512221951526981', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251222195152072413', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 151.00, '待退款', '2025-12-22 19:51:52', '2025-12-24 23:30:04');
INSERT INTO `orders` VALUES (69, 3, 'ORD202512231049251434', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223104925048985', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '待审核', '2025-12-23 10:49:25', '2025-12-23 11:14:07');
INSERT INTO `orders` VALUES (70, 3, 'ORD202512231607146768', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223160714493467', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '待审核', '2025-12-23 16:07:15', '2025-12-23 19:18:41');
INSERT INTO `orders` VALUES (71, 3, 'ORD202512231619343432', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223161934823526', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 190.95, '待审核', '2025-12-23 16:19:35', '2025-12-23 18:56:12');
INSERT INTO `orders` VALUES (72, 3, 'ORD202512231620312834', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223162031502362', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 190.95, '待退款', '2025-12-23 16:20:32', '2026-01-04 17:13:18');
INSERT INTO `orders` VALUES (73, 3, 'ORD202512231658168232', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223165816475370', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 190.95, '待审核', '2025-12-23 16:58:16', '2025-12-23 18:48:04');
INSERT INTO `orders` VALUES (74, 3, '2025122322001426530508094572', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223171120542202', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 190.95, '待出行', '2025-12-23 17:11:21', '2026-01-04 17:13:34');
INSERT INTO `orders` VALUES (75, 3, '2025122322001426530508096488', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223173943905170', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '已完成', '2025-12-23 17:39:44', '2025-12-23 19:15:51');
INSERT INTO `orders` VALUES (76, 3, 'ORD202512231759154459', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223175915947651', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 190.95, '待审核', '2025-12-23 17:59:16', '2025-12-23 18:51:47');
INSERT INTO `orders` VALUES (77, 3, 'ORD202512231800371558', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223180037551430', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 190.95, '待审核', '2025-12-23 18:00:38', '2025-12-23 18:40:43');
INSERT INTO `orders` VALUES (78, 3, 'ORD20251223200833891', '普通乘客', '北京首都国际机场 → 上海虹桥国际机场', 'HU7615', 'TKT20251223200833894753', '2025-12-23 20:08:34', NULL, 1.00, '待审核', '2025-12-23 20:08:34', '2025-12-23 20:08:34');
INSERT INTO `orders` VALUES (80, 3, 'ORD20251223204249253', '普通乘客', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223204249334105', '2025-12-23 20:42:50', '2025-12-09 12:00:00', 1.00, '待处理', '2025-12-23 20:42:50', '2025-12-23 22:46:31');
INSERT INTO `orders` VALUES (81, 3, '2025122322001426530508088246', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223210032844280', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '待处理', '2025-12-23 21:00:32', '2025-12-23 21:14:50');
INSERT INTO `orders` VALUES (82, 3, 'ORD20251223211450788', '普通乘客', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223211450060646', '2025-12-23 21:14:50', '2025-12-09 12:00:00', 201.00, '待审核', '2025-12-23 21:14:50', '2025-12-23 21:14:50');
INSERT INTO `orders` VALUES (83, 3, '2025122322001426530508089546', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223211640895694', '2026-01-03 08:00:00', '2026-01-03 12:00:00', 201.00, '待出行', '2025-12-23 21:16:40', '2025-12-30 10:43:59');
INSERT INTO `orders` VALUES (84, 3, '2025122322001426530508091002', '李好呀呀呀', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223211937743693', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '已完成', '2025-12-23 21:19:38', '2025-12-25 18:23:53');
INSERT INTO `orders` VALUES (85, 3, 'ORD20251223224630214', '普通乘客', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251223224630740929', '2025-12-23 22:46:31', '2025-12-09 12:00:00', 1.00, '待审核', '2025-12-23 22:46:31', '2025-12-23 22:46:31');
INSERT INTO `orders` VALUES (86, 3, 'ORD20251224102409234', '普通乘客', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251224102409002753', '2025-12-24 10:24:10', '2025-12-09 12:00:00', 1.00, '已完成', '2025-12-24 10:24:10', '2026-01-04 17:12:43');
INSERT INTO `orders` VALUES (87, 3, '2025122422001426530508089548', '周瑾', '北京首都国际机场 → 上海虹桥国际机场', 'HU7615', 'TKT20251224103820191450', '2025-12-09 14:00:00', '2025-12-09 18:00:00', 953.32, '待审核', '2025-12-24 10:38:21', '2025-12-24 12:35:11');
INSERT INTO `orders` VALUES (89, 3, '2025122422001426530508101430', '周瑾', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251224130157221442', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 138.70, '待审核', '2025-12-24 13:01:58', '2025-12-28 21:13:53');
INSERT INTO `orders` VALUES (90, 3, '2025122422001426530508093274', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251224131203059254', '2026-01-03 08:00:00', '2026-01-03 12:00:00', 151.00, '待出行', '2025-12-24 13:12:03', '2025-12-30 10:43:18');
INSERT INTO `orders` VALUES (91, 3, '2025122422001426530508098298', '周瑾', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251224131611649060', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 151.00, '已完成', '2025-12-24 13:16:11', '2025-12-24 23:34:18');
INSERT INTO `orders` VALUES (92, 3, '2025122422001426530508099765', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251224132502411690', '2026-01-09 08:00:00', '2026-01-09 12:00:00', 151.00, '待出行', '2025-12-24 13:25:02', '2025-12-30 10:42:31');
INSERT INTO `orders` VALUES (93, 3, '2025122422001426530508101431', '张建', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251224132641488264', '2026-01-09 08:00:00', '2026-01-09 12:00:00', 151.00, '待处理', '2025-12-24 13:26:42', '2026-01-04 17:41:26');
INSERT INTO `orders` VALUES (94, 3, 'ORD20251224134647972', '普通乘客', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251224134647236804', '2025-12-24 13:46:47', '2025-12-09 12:00:00', 151.00, 'deleted', '2025-12-24 13:46:47', '2025-12-25 18:23:13');
INSERT INTO `orders` VALUES (95, 3, 'ORD20251224135438198', '普通乘客', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251224135438843551', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 151.00, '驳回', '2025-12-24 13:54:39', '2025-12-24 23:42:19');
INSERT INTO `orders` VALUES (97, 3, '2025122522001426530508111952', '李好呀3', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251225175302094541', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 151.00, '已完成', '2025-12-25 17:53:02', '2025-12-25 19:24:27');
INSERT INTO `orders` VALUES (98, 3, '2025122522001426530508113155', '李好呀3', '上海虹桥国际机场 → 成都天府国际机场', 'HO1234', 'TKT20251225180233802651', '2025-12-22 16:00:00', '2025-12-22 20:00:00', 1220.00, '已完成', '2025-12-25 18:02:34', '2025-12-25 18:41:02');
INSERT INTO `orders` VALUES (99, 3, 'ORD20251225183950527', '李好呀3', '上海虹桥国际机场 → 成都天府国际机场', 'HO1234', 'TKT20251225183950841370', '2025-12-22 16:00:00', '2025-12-22 20:00:00', 1159.00, '待审核', '2025-12-25 18:39:51', '2025-12-25 18:43:53');
INSERT INTO `orders` VALUES (100, 3, 'ORD20251225192259863', '李好呀3', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251225192259099157', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 151.00, '待处理', '2025-12-25 19:22:59', '2026-01-04 17:38:09');
INSERT INTO `orders` VALUES (101, 3, 'ORD20251226001807354', '李好呀3', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251226001807084275', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 151.00, '驳回', '2025-12-26 00:18:08', '2026-01-04 17:12:58');
INSERT INTO `orders` VALUES (102, 20, 'ORD_INTL_20251226_1', 'Intl Passenger 1', 'LHR → PEK', 'INTL1001', NULL, '2025-12-30 09:00:00', '2025-12-30 23:30:00', 4620.00, 'paid', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `orders` VALUES (103, 21, 'ORD_INTL_20251226_2', 'Intl Passenger 2', 'JFK → PVG', 'INTL2002', NULL, '2025-12-31 10:00:00', '2026-01-01 02:30:00', 5280.00, 'paid', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `orders` VALUES (104, 22, 'ORD_INTL_20251226_3', 'Intl Passenger 3', 'NRT → CAN', 'INTL3003', NULL, '2025-12-29 13:00:00', '2025-12-29 18:30:00', 3520.00, 'paid', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `orders` VALUES (105, 23, 'ORD_INTL_20251226_4', 'Intl Passenger 4', 'SYD → SHA', 'INTL4004', NULL, '2025-12-28 08:00:00', '2025-12-28 20:30:00', 6050.00, 'paid', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `orders` VALUES (106, 24, 'ORD_INTL_20251226_5', 'Intl Passenger 5', 'DXB → SZX', 'INTL5005', NULL, '2025-12-27 02:00:00', '2025-12-27 14:30:00', 4290.00, 'paid', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `orders` VALUES (107, 3, '2025122722001426530508142492', '李好呀3', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251227223511070313', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '已完成', '2025-12-27 22:35:12', '2025-12-27 22:45:03');
INSERT INTO `orders` VALUES (108, 3, 'ORD20251227224406449', '李好呀3', '北京首都国际机场 → 上海虹桥国际机场', 'CA1201', 'TKT20251227224406865767', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 201.00, '已完成', '2025-12-27 22:44:06', '2025-12-28 21:17:31');
INSERT INTO `orders` VALUES (109, 3, '2025122822001426530508146267', '你好呀', 'PKX → PVG', 'CA1201', 'TKT20251228202148572160', '2025-12-09 08:00:00', '2025-12-09 12:00:00', 151.00, 'deleted', '2025-12-28 20:21:48', '2025-12-28 20:24:58');
INSERT INTO `orders` VALUES (110, 3, 'ORD20251228211536486', '李好呀3', '北京首都国际机场 → 上海虹桥国际机场', 'HU7615', 'TKT20251228211536945213', '2025-12-09 14:00:00', '2025-12-09 12:00:00', 201.00, '已完成', '2025-12-28 21:15:37', '2026-01-04 15:08:35');
INSERT INTO `orders` VALUES (111, 3, 'ORD20251229171752243', '普通乘客', '北京首都国际机场 → 上海虹桥国际机场', 'HU7615', 'TKT20251229171752932962', '2025-12-09 14:00:00', '2025-12-09 12:00:00', 1.00, '待支付', '2025-12-29 17:17:53', '2026-01-04 17:12:43');
INSERT INTO `orders` VALUES (112, 3, 'ORD20251230100621191', '李好呀3', '北京首都国际机场 → 上海虹桥国际机场', 'HU7615', 'TKT20251230100621834774', '2025-12-09 14:00:00', '2025-12-09 12:00:00', 201.00, '待支付', '2025-12-30 10:06:22', '2026-01-04 15:08:35');
INSERT INTO `orders` VALUES (113, 3, '2026010322001426530508203237', '张浩然', 'PEK → SHA', 'CA1501', 'TKT20260103113704209041', '2026-01-07 08:30:00', '2026-01-07 10:55:00', 2128.50, '待退款', '2026-01-03 11:37:05', '2026-01-03 13:42:05');
INSERT INTO `orders` VALUES (114, 3, '2026010322001426530508203238', '张建', 'PEK → SHA', 'CA1501', 'TKT20260103113815051136', '2026-01-07 08:30:00', '2026-01-07 10:55:00', 2365.00, '已完成', '2026-01-03 11:38:16', '2026-01-03 13:40:59');
INSERT INTO `orders` VALUES (115, 3, 'ORD20260103134003624', '张建', 'PEK → SHA', 'CA1519', 'TKT20260103134003851217', '2026-01-07 09:30:00', '2026-01-07 10:55:00', 2150.00, 'deleted', '2026-01-03 13:40:04', '2026-01-04 15:06:00');

-- ----------------------------
-- Table structure for seats
-- ----------------------------
DROP TABLE IF EXISTS `seats`;
CREATE TABLE `seats`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '座位ID',
  `flight_id` int NOT NULL COMMENT '航班ID，关联flights表',
  `seat_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位号，如：1A, 1B, 2C等',
  `cabin_class` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '舱位等级',
  `rownumber` int NOT NULL COMMENT '行号（排号）',
  `seat_position` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位位置，如：A, B, C, D, E, F等',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '座位价格（单位：元），不同舱位和位置可能有不同价格',
  `available_count` int NOT NULL DEFAULT 1 COMMENT '余票数量，通常为1（每个座位只能被一人占用），但某些座位可能允许多人共享',
  `status` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '座位状态：available-可用, occupied-已占用, reserved-已预订, maintenance-维护中',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_flight_seat_number`(`flight_id` ASC, `seat_number` ASC) USING BTREE,
  INDEX `idx_flight_id`(`flight_id` ASC) USING BTREE,
  INDEX `idx_cabin_class`(`cabin_class` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_row_number`(`rownumber` ASC) USING BTREE,
  INDEX `idx_flight_cabin_status`(`flight_id` ASC, `cabin_class` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_seats_flight` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 629 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of seats
-- ----------------------------
INSERT INTO `seats` VALUES (1, 14, '1A', '头等舱', 1, 'A', 415.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:36');
INSERT INTO `seats` VALUES (2, 14, '1C', '头等舱', 1, 'C', 415.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:36');
INSERT INTO `seats` VALUES (3, 14, '1D', '头等舱', 1, 'D', 415.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:36');
INSERT INTO `seats` VALUES (4, 14, '1F', '头等舱', 1, 'F', 415.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:36');
INSERT INTO `seats` VALUES (5, 14, '2A', '头等舱', 2, 'A', 415.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 11:47:36');
INSERT INTO `seats` VALUES (6, 14, '2C', '头等舱', 2, 'C', 415.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:36');
INSERT INTO `seats` VALUES (7, 14, '2D', '头等舱', 2, 'D', 415.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:36');
INSERT INTO `seats` VALUES (8, 14, '2F', '头等舱', 2, 'F', 415.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:36');
INSERT INTO `seats` VALUES (9, 14, '3A', '商务舱', 3, 'A', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (10, 14, '3B', '商务舱', 3, 'B', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (11, 14, '3D', '商务舱', 3, 'D', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (12, 14, '3E', '商务舱', 3, 'E', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (13, 14, '4A', '商务舱', 4, 'A', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (14, 14, '4B', '商务舱', 4, 'B', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (15, 14, '4D', '商务舱', 4, 'D', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (16, 14, '4E', '商务舱', 4, 'E', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (17, 14, '5A', '商务舱', 5, 'A', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (18, 14, '5B', '商务舱', 5, 'B', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (19, 14, '5D', '商务舱', 5, 'D', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (20, 14, '5E', '商务舱', 5, 'E', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (21, 14, '6A', '商务舱', 6, 'A', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (22, 14, '6B', '商务舱', 6, 'B', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (23, 14, '6D', '商务舱', 6, 'D', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (24, 14, '6E', '商务舱', 6, 'E', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (25, 14, '7A', '商务舱', 7, 'A', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (26, 14, '7B', '商务舱', 7, 'B', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (27, 14, '7D', '商务舱', 7, 'D', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (28, 14, '7E', '商务舱', 7, 'E', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (29, 14, '8A', '商务舱', 8, 'A', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (30, 14, '8B', '商务舱', 8, 'B', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (31, 14, '8D', '商务舱', 8, 'D', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (32, 14, '8E', '商务舱', 8, 'E', 315.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:29');
INSERT INTO `seats` VALUES (33, 14, '9A', '经济舱', 9, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (34, 14, '9B', '经济舱', 9, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (35, 14, '9C', '经济舱', 9, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (36, 14, '9D', '经济舱', 9, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (37, 14, '9E', '经济舱', 9, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (39, 14, '10A', '经济舱', 10, 'A', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 20:39:52');
INSERT INTO `seats` VALUES (40, 14, '10B', '经济舱', 10, 'B', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 20:58:41');
INSERT INTO `seats` VALUES (41, 14, '10C', '经济舱', 10, 'C', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 21:04:03');
INSERT INTO `seats` VALUES (42, 14, '10D', '经济舱', 10, 'D', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 21:06:16');
INSERT INTO `seats` VALUES (43, 14, '10E', '经济舱', 10, 'E', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 21:12:07');
INSERT INTO `seats` VALUES (44, 14, '10F', '经济舱', 10, 'F', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 21:32:06');
INSERT INTO `seats` VALUES (45, 14, '11A', '经济舱', 11, 'A', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 21:36:49');
INSERT INTO `seats` VALUES (46, 14, '11B', '经济舱', 11, 'B', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 22:02:50');
INSERT INTO `seats` VALUES (47, 14, '11C', '经济舱', 11, 'C', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 22:05:27');
INSERT INTO `seats` VALUES (48, 14, '11D', '经济舱', 11, 'D', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-18 22:15:52');
INSERT INTO `seats` VALUES (49, 14, '11E', '经济舱', 11, 'E', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-19 08:10:14');
INSERT INTO `seats` VALUES (50, 14, '11F', '经济舱', 11, 'F', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 13:53:21');
INSERT INTO `seats` VALUES (51, 14, '12A', '经济舱', 12, 'A', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 14:00:55');
INSERT INTO `seats` VALUES (52, 14, '12B', '经济舱', 12, 'B', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 14:13:48');
INSERT INTO `seats` VALUES (53, 14, '12C', '经济舱', 12, 'C', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 14:18:47');
INSERT INTO `seats` VALUES (54, 14, '12D', '经济舱', 12, 'D', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 14:19:49');
INSERT INTO `seats` VALUES (55, 14, '12E', '经济舱', 12, 'E', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 14:25:18');
INSERT INTO `seats` VALUES (56, 14, '12F', '经济舱', 12, 'F', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 14:31:17');
INSERT INTO `seats` VALUES (57, 14, '13A', '经济舱', 13, 'A', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 16:00:08');
INSERT INTO `seats` VALUES (58, 14, '13B', '经济舱', 13, 'B', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 19:17:17');
INSERT INTO `seats` VALUES (59, 14, '13C', '经济舱', 13, 'C', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 20:06:26');
INSERT INTO `seats` VALUES (60, 14, '13D', '经济舱', 13, 'D', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-23 20:07:23');
INSERT INTO `seats` VALUES (61, 14, '13E', '经济舱', 13, 'E', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-24 10:34:33');
INSERT INTO `seats` VALUES (62, 14, '13F', '经济舱', 13, 'F', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-28 21:14:32');
INSERT INTO `seats` VALUES (63, 14, '14A', '经济舱', 14, 'A', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-29 17:17:12');
INSERT INTO `seats` VALUES (64, 14, '14B', '经济舱', 14, 'B', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2025-12-30 10:05:38');
INSERT INTO `seats` VALUES (65, 14, '14C', '经济舱', 14, 'C', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2026-01-03 11:34:08');
INSERT INTO `seats` VALUES (66, 14, '14D', '经济舱', 14, 'D', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2026-01-03 11:36:23');
INSERT INTO `seats` VALUES (67, 14, '14E', '经济舱', 14, 'E', 215.00, 1, '已占用', '2025-12-18 11:38:03', '2026-01-03 11:37:33');
INSERT INTO `seats` VALUES (68, 14, '14F', '经济舱', 14, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (69, 14, '15A', '经济舱', 15, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (70, 14, '15B', '经济舱', 15, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (71, 14, '15C', '经济舱', 15, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (72, 14, '15D', '经济舱', 15, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (73, 14, '15E', '经济舱', 15, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (74, 14, '15F', '经济舱', 15, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (75, 14, '16A', '经济舱', 16, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (76, 14, '16B', '经济舱', 16, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (77, 14, '16C', '经济舱', 16, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (78, 14, '16D', '经济舱', 16, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (79, 14, '16E', '经济舱', 16, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (80, 14, '16F', '经济舱', 16, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (81, 14, '17A', '经济舱', 17, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (82, 14, '17B', '经济舱', 17, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (83, 14, '17C', '经济舱', 17, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (84, 14, '17D', '经济舱', 17, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (85, 14, '17E', '经济舱', 17, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (86, 14, '17F', '经济舱', 17, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (87, 14, '18A', '经济舱', 18, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (88, 14, '18B', '经济舱', 18, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (89, 14, '18C', '经济舱', 18, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (90, 14, '18D', '经济舱', 18, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (91, 14, '18E', '经济舱', 18, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (92, 14, '18F', '经济舱', 18, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (93, 14, '19A', '经济舱', 19, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (94, 14, '19B', '经济舱', 19, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (95, 14, '19C', '经济舱', 19, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (96, 14, '19D', '经济舱', 19, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (97, 14, '19E', '经济舱', 19, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (98, 14, '19F', '经济舱', 19, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (99, 14, '20A', '经济舱', 20, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (100, 14, '20B', '经济舱', 20, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (101, 14, '20C', '经济舱', 20, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (102, 14, '20D', '经济舱', 20, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (103, 14, '20E', '经济舱', 20, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (104, 14, '20F', '经济舱', 20, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (105, 14, '21A', '经济舱', 21, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (106, 14, '21B', '经济舱', 21, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (107, 14, '21C', '经济舱', 21, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (108, 14, '21D', '经济舱', 21, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (109, 14, '21E', '经济舱', 21, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (110, 14, '21F', '经济舱', 21, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (111, 14, '22A', '经济舱', 22, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (112, 14, '22B', '经济舱', 22, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (113, 14, '22C', '经济舱', 22, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (114, 14, '22D', '经济舱', 22, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (115, 14, '22E', '经济舱', 22, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (116, 14, '22F', '经济舱', 22, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (117, 14, '23A', '经济舱', 23, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (118, 14, '23B', '经济舱', 23, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (119, 14, '23C', '经济舱', 23, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (120, 14, '23D', '经济舱', 23, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (121, 14, '23E', '经济舱', 23, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (122, 14, '23F', '经济舱', 23, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (123, 14, '24A', '经济舱', 24, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (124, 14, '24B', '经济舱', 24, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (125, 14, '24C', '经济舱', 24, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (126, 14, '24D', '经济舱', 24, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (127, 14, '24E', '经济舱', 24, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (128, 14, '24F', '经济舱', 24, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (129, 14, '25A', '经济舱', 25, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (130, 14, '25B', '经济舱', 25, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (131, 14, '25C', '经济舱', 25, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (132, 14, '25D', '经济舱', 25, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (133, 14, '25E', '经济舱', 25, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (134, 14, '25F', '经济舱', 25, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (135, 14, '26A', '经济舱', 26, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (136, 14, '26B', '经济舱', 26, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (137, 14, '26C', '经济舱', 26, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (138, 14, '26D', '经济舱', 26, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (139, 14, '26E', '经济舱', 26, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (140, 14, '26F', '经济舱', 26, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (141, 14, '27A', '经济舱', 27, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (142, 14, '27B', '经济舱', 27, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (143, 14, '27C', '经济舱', 27, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (144, 14, '27D', '经济舱', 27, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (145, 14, '27E', '经济舱', 27, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (146, 14, '27F', '经济舱', 27, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (147, 14, '28A', '经济舱', 28, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (148, 14, '28B', '经济舱', 28, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (149, 14, '28C', '经济舱', 28, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (150, 14, '28D', '经济舱', 28, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (151, 14, '28E', '经济舱', 28, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (152, 14, '28F', '经济舱', 28, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (153, 14, '29A', '经济舱', 29, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (154, 14, '29B', '经济舱', 29, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (155, 14, '29C', '经济舱', 29, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (156, 14, '29D', '经济舱', 29, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (157, 14, '29E', '经济舱', 29, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (158, 14, '29F', '经济舱', 29, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (159, 14, '30A', '经济舱', 30, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (160, 14, '30B', '经济舱', 30, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (161, 14, '30C', '经济舱', 30, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (162, 14, '30D', '经济舱', 30, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (163, 14, '30E', '经济舱', 30, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (164, 14, '30F', '经济舱', 30, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (165, 14, '31A', '经济舱', 31, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (166, 14, '31B', '经济舱', 31, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (167, 14, '31C', '经济舱', 31, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (168, 14, '31D', '经济舱', 31, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (169, 14, '31E', '经济舱', 31, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (170, 14, '31F', '经济舱', 31, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (171, 14, '32A', '经济舱', 32, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (172, 14, '32B', '经济舱', 32, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (173, 14, '32C', '经济舱', 32, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (174, 14, '32D', '经济舱', 32, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (175, 14, '32E', '经济舱', 32, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (176, 14, '32F', '经济舱', 32, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (177, 14, '33A', '经济舱', 33, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (178, 14, '33B', '经济舱', 33, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (179, 14, '33C', '经济舱', 33, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (180, 14, '33D', '经济舱', 33, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (181, 14, '33E', '经济舱', 33, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (182, 14, '33F', '经济舱', 33, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (183, 14, '34A', '经济舱', 34, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (184, 14, '34B', '经济舱', 34, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (185, 14, '34C', '经济舱', 34, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (186, 14, '34D', '经济舱', 34, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (187, 14, '34E', '经济舱', 34, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (188, 14, '34F', '经济舱', 34, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (189, 14, '35A', '经济舱', 35, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (190, 14, '35B', '经济舱', 35, 'B', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (191, 14, '35C', '经济舱', 35, 'C', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (192, 14, '35D', '经济舱', 35, 'D', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (193, 14, '35E', '经济舱', 35, 'E', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (194, 14, '35F', '经济舱', 35, 'F', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (195, 14, '36A', '经济舱', 36, 'A', 215.00, 1, '可用', '2025-12-18 11:38:03', '2025-12-18 11:47:23');
INSERT INTO `seats` VALUES (196, 11, '1A', '商务舱', 1, 'A', 300.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:16');
INSERT INTO `seats` VALUES (197, 11, '1B', '商务舱', 1, 'B', 300.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:16');
INSERT INTO `seats` VALUES (198, 11, '1C', '商务舱', 1, 'C', 300.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:16');
INSERT INTO `seats` VALUES (199, 11, '1D', '商务舱', 1, 'D', 300.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:16');
INSERT INTO `seats` VALUES (200, 11, '2A', '商务舱', 2, 'A', 300.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-18 11:45:16');
INSERT INTO `seats` VALUES (201, 11, '2B', '商务舱', 2, 'B', 300.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:16');
INSERT INTO `seats` VALUES (202, 11, '2C', '商务舱', 2, 'C', 300.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:16');
INSERT INTO `seats` VALUES (203, 11, '2D', '商务舱', 2, 'D', 300.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:16');
INSERT INTO `seats` VALUES (204, 11, '3A', '头等舱', 3, 'A', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (205, 11, '3B', '头等舱', 3, 'B', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (206, 11, '3C', '头等舱', 3, 'C', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (207, 11, '3D', '头等舱', 3, 'D', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (208, 11, '3E', '头等舱', 3, 'E', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (209, 11, '3F', '头等舱', 3, 'F', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (210, 11, '4A', '头等舱', 4, 'A', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 20:10:19');
INSERT INTO `seats` VALUES (211, 11, '4B', '头等舱', 4, 'B', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (212, 11, '4C', '头等舱', 4, 'C', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (213, 11, '4D', '头等舱', 4, 'D', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (214, 11, '4E', '头等舱', 4, 'E', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (215, 11, '4F', '头等舱', 4, 'F', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (216, 11, '5A', '头等舱', 5, 'A', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (217, 11, '5B', '头等舱', 5, 'B', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (218, 11, '5C', '头等舱', 5, 'C', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (219, 11, '5D', '头等舱', 5, 'D', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (220, 11, '5E', '头等舱', 5, 'E', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (221, 11, '5F', '头等舱', 5, 'F', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (222, 11, '6A', '头等舱', 6, 'A', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (223, 11, '6B', '头等舱', 6, 'B', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (224, 11, '6C', '头等舱', 6, 'C', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (225, 11, '6D', '头等舱', 6, 'D', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (226, 11, '6E', '头等舱', 6, 'E', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (227, 11, '6F', '头等舱', 6, 'F', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (228, 11, '7A', '头等舱', 7, 'A', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (229, 11, '7B', '头等舱', 7, 'B', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (230, 11, '7C', '头等舱', 7, 'C', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (231, 11, '7D', '头等舱', 7, 'D', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (232, 11, '7E', '头等舱', 7, 'E', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (233, 11, '7F', '头等舱', 7, 'F', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (234, 11, '8A', '头等舱', 8, 'A', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (235, 11, '8B', '头等舱', 8, 'B', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (236, 11, '8C', '头等舱', 8, 'C', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (237, 11, '8D', '头等舱', 8, 'D', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (238, 11, '8E', '头等舱', 8, 'E', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (239, 11, '8F', '头等舱', 8, 'F', 400.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:22');
INSERT INTO `seats` VALUES (240, 11, '9A', '经济舱', 9, 'A', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (241, 11, '9B', '经济舱', 9, 'B', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (242, 11, '9C', '经济舱', 9, 'C', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (243, 11, '9D', '经济舱', 9, 'D', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (244, 11, '9E', '经济舱', 9, 'E', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (246, 11, '10A', '经济舱', 10, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-20 15:03:08');
INSERT INTO `seats` VALUES (247, 11, '10B', '经济舱', 10, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-20 16:15:05');
INSERT INTO `seats` VALUES (248, 11, '10C', '经济舱', 10, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-20 16:32:39');
INSERT INTO `seats` VALUES (249, 11, '10D', '经济舱', 10, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-20 16:49:59');
INSERT INTO `seats` VALUES (250, 11, '10E', '经济舱', 10, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-20 18:45:50');
INSERT INTO `seats` VALUES (251, 11, '10F', '经济舱', 10, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-20 18:47:28');
INSERT INTO `seats` VALUES (252, 11, '11A', '经济舱', 11, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-20 18:50:34');
INSERT INTO `seats` VALUES (253, 11, '11B', '经济舱', 11, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-20 19:04:00');
INSERT INTO `seats` VALUES (254, 11, '11C', '经济舱', 11, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-21 14:04:52');
INSERT INTO `seats` VALUES (255, 11, '11D', '经济舱', 11, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-21 16:59:37');
INSERT INTO `seats` VALUES (256, 11, '11E', '经济舱', 11, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-21 21:29:14');
INSERT INTO `seats` VALUES (257, 11, '11F', '经济舱', 11, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-21 21:32:23');
INSERT INTO `seats` VALUES (258, 11, '12A', '经济舱', 12, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-22 17:51:32');
INSERT INTO `seats` VALUES (259, 11, '12B', '经济舱', 12, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-22 19:51:14');
INSERT INTO `seats` VALUES (260, 11, '12C', '经济舱', 12, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-22 20:07:19');
INSERT INTO `seats` VALUES (261, 11, '12D', '经济舱', 12, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-22 20:15:55');
INSERT INTO `seats` VALUES (262, 11, '12E', '经济舱', 12, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 10:48:35');
INSERT INTO `seats` VALUES (263, 11, '12F', '经济舱', 12, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 16:00:32');
INSERT INTO `seats` VALUES (264, 11, '13A', '经济舱', 13, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 16:06:36');
INSERT INTO `seats` VALUES (265, 11, '13B', '经济舱', 13, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 16:19:06');
INSERT INTO `seats` VALUES (266, 11, '13C', '经济舱', 13, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 16:19:51');
INSERT INTO `seats` VALUES (267, 11, '13D', '经济舱', 13, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 16:52:58');
INSERT INTO `seats` VALUES (268, 11, '13E', '经济舱', 13, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 16:57:19');
INSERT INTO `seats` VALUES (269, 11, '13F', '经济舱', 13, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 17:10:27');
INSERT INTO `seats` VALUES (270, 11, '14A', '经济舱', 14, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 17:37:52');
INSERT INTO `seats` VALUES (271, 11, '14B', '经济舱', 14, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 17:58:36');
INSERT INTO `seats` VALUES (272, 11, '14C', '经济舱', 14, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 17:59:58');
INSERT INTO `seats` VALUES (273, 11, '14D', '经济舱', 14, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 19:44:15');
INSERT INTO `seats` VALUES (274, 11, '14E', '经济舱', 14, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 19:46:52');
INSERT INTO `seats` VALUES (275, 11, '14F', '经济舱', 14, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 20:29:12');
INSERT INTO `seats` VALUES (276, 11, '15A', '经济舱', 15, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 20:34:54');
INSERT INTO `seats` VALUES (277, 11, '15B', '经济舱', 15, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 20:41:43');
INSERT INTO `seats` VALUES (278, 11, '15C', '经济舱', 15, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 20:45:55');
INSERT INTO `seats` VALUES (279, 11, '15D', '经济舱', 15, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 20:59:38');
INSERT INTO `seats` VALUES (280, 11, '15E', '经济舱', 15, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 20:59:53');
INSERT INTO `seats` VALUES (281, 11, '15F', '经济舱', 15, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 21:10:49');
INSERT INTO `seats` VALUES (282, 11, '16A', '经济舱', 16, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 21:13:59');
INSERT INTO `seats` VALUES (283, 11, '16B', '经济舱', 16, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 21:16:04');
INSERT INTO `seats` VALUES (284, 11, '16C', '经济舱', 16, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 21:18:00');
INSERT INTO `seats` VALUES (285, 11, '16D', '经济舱', 16, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-23 22:45:35');
INSERT INTO `seats` VALUES (286, 11, '16E', '经济舱', 16, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 10:21:41');
INSERT INTO `seats` VALUES (287, 11, '16F', '经济舱', 16, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 10:22:58');
INSERT INTO `seats` VALUES (288, 11, '17A', '经济舱', 17, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 12:37:59');
INSERT INTO `seats` VALUES (289, 11, '17B', '经济舱', 17, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 12:51:23');
INSERT INTO `seats` VALUES (290, 11, '17C', '经济舱', 17, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:09:47');
INSERT INTO `seats` VALUES (291, 11, '17D', '经济舱', 17, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:13:07');
INSERT INTO `seats` VALUES (292, 11, '17E', '经济舱', 17, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:15:30');
INSERT INTO `seats` VALUES (293, 11, '17F', '经济舱', 17, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:22:19');
INSERT INTO `seats` VALUES (294, 11, '18A', '经济舱', 18, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:26:08');
INSERT INTO `seats` VALUES (295, 11, '18B', '经济舱', 18, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:34:09');
INSERT INTO `seats` VALUES (296, 11, '18C', '经济舱', 18, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:40:47');
INSERT INTO `seats` VALUES (297, 11, '18D', '经济舱', 18, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:43:11');
INSERT INTO `seats` VALUES (298, 11, '18E', '经济舱', 18, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:45:51');
INSERT INTO `seats` VALUES (299, 11, '18F', '经济舱', 18, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 13:53:30');
INSERT INTO `seats` VALUES (300, 11, '19A', '经济舱', 19, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-24 14:30:24');
INSERT INTO `seats` VALUES (301, 11, '19B', '经济舱', 19, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-25 17:52:09');
INSERT INTO `seats` VALUES (302, 11, '19C', '经济舱', 19, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-25 19:21:31');
INSERT INTO `seats` VALUES (303, 11, '19D', '经济舱', 19, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-26 00:17:06');
INSERT INTO `seats` VALUES (304, 11, '19E', '经济舱', 19, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-27 22:33:51');
INSERT INTO `seats` VALUES (305, 11, '19F', '经济舱', 19, 'F', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-27 22:40:53');
INSERT INTO `seats` VALUES (306, 11, '20A', '经济舱', 20, 'A', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-27 22:43:13');
INSERT INTO `seats` VALUES (307, 11, '20B', '经济舱', 20, 'B', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2025-12-28 20:20:25');
INSERT INTO `seats` VALUES (308, 11, '20C', '经济舱', 20, 'C', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2026-01-03 11:55:25');
INSERT INTO `seats` VALUES (309, 11, '20D', '经济舱', 20, 'D', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2026-01-03 13:36:59');
INSERT INTO `seats` VALUES (310, 11, '20E', '经济舱', 20, 'E', 200.00, 1, '已占用', '2025-12-18 11:38:50', '2026-01-03 13:38:45');
INSERT INTO `seats` VALUES (311, 11, '20F', '经济舱', 20, 'F', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (312, 11, '21A', '经济舱', 21, 'A', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (313, 11, '21B', '经济舱', 21, 'B', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (314, 11, '21C', '经济舱', 21, 'C', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (315, 11, '21D', '经济舱', 21, 'D', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (316, 11, '21E', '经济舱', 21, 'E', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (317, 11, '21F', '经济舱', 21, 'F', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (318, 11, '22A', '经济舱', 22, 'A', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (319, 11, '22B', '经济舱', 22, 'B', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (320, 11, '22C', '经济舱', 22, 'C', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (321, 11, '22D', '经济舱', 22, 'D', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (322, 11, '22E', '经济舱', 22, 'E', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (323, 11, '22F', '经济舱', 22, 'F', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (324, 11, '23A', '经济舱', 23, 'A', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (325, 11, '23B', '经济舱', 23, 'B', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (326, 11, '23C', '经济舱', 23, 'C', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (327, 11, '23D', '经济舱', 23, 'D', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (328, 11, '23E', '经济舱', 23, 'E', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (329, 11, '23F', '经济舱', 23, 'F', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (330, 11, '24A', '经济舱', 24, 'A', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (331, 11, '24B', '经济舱', 24, 'B', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (332, 11, '24C', '经济舱', 24, 'C', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (333, 11, '24D', '经济舱', 24, 'D', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (334, 11, '24E', '经济舱', 24, 'E', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (335, 11, '24F', '经济舱', 24, 'F', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (336, 11, '25A', '经济舱', 25, 'A', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (337, 11, '25B', '经济舱', 25, 'B', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (338, 11, '25C', '经济舱', 25, 'C', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (339, 11, '25D', '经济舱', 25, 'D', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (340, 11, '25E', '经济舱', 25, 'E', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (341, 11, '25F', '经济舱', 25, 'F', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (342, 11, '26A', '经济舱', 26, 'A', 200.00, 1, '可用', '2025-12-18 11:38:50', '2025-12-18 11:45:09');
INSERT INTO `seats` VALUES (343, 12, '1A', '商务舱', 1, 'A', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (344, 12, '1B', '商务舱', 1, 'B', 305.00, 1, '已占用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (345, 12, '1C', '商务舱', 1, 'C', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (346, 12, '1D', '商务舱', 1, 'D', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (347, 12, '2A', '商务舱', 2, 'A', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (348, 12, '2B', '商务舱', 2, 'B', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (349, 12, '2C', '商务舱', 2, 'C', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (350, 12, '2D', '商务舱', 2, 'D', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (351, 12, '3A', '商务舱', 3, 'A', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (352, 12, '3B', '商务舱', 3, 'B', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (353, 12, '3C', '商务舱', 3, 'C', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (354, 12, '3D', '商务舱', 3, 'D', 305.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:14');
INSERT INTO `seats` VALUES (355, 12, '4A', '头等舱', 4, 'A', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (356, 12, '4B', '头等舱', 4, 'B', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (357, 12, '4C', '头等舱', 4, 'C', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (358, 12, '4D', '头等舱', 4, 'D', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (359, 12, '4E', '头等舱', 4, 'E', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (360, 12, '4F', '头等舱', 4, 'F', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (361, 12, '5A', '头等舱', 5, 'A', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (362, 12, '5B', '头等舱', 5, 'B', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (363, 12, '5C', '头等舱', 5, 'C', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (364, 12, '5D', '头等舱', 5, 'D', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (365, 12, '5E', '头等舱', 5, 'E', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (366, 12, '5F', '头等舱', 5, 'F', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (367, 12, '6A', '头等舱', 6, 'A', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (368, 12, '6B', '头等舱', 6, 'B', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (369, 12, '6C', '头等舱', 6, 'C', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (370, 12, '6D', '头等舱', 6, 'D', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (371, 12, '6E', '头等舱', 6, 'E', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (372, 12, '6F', '头等舱', 6, 'F', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (373, 12, '7A', '头等舱', 7, 'A', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (374, 12, '7B', '头等舱', 7, 'B', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (375, 12, '7C', '头等舱', 7, 'C', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (376, 12, '7D', '头等舱', 7, 'D', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (377, 12, '7E', '头等舱', 7, 'E', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (378, 12, '7F', '头等舱', 7, 'F', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (379, 12, '8A', '头等舱', 8, 'A', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (380, 12, '8B', '头等舱', 8, 'B', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (381, 12, '8C', '头等舱', 8, 'C', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (382, 12, '8D', '头等舱', 8, 'D', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (383, 12, '8E', '头等舱', 8, 'E', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (384, 12, '8F', '头等舱', 8, 'F', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (385, 12, '9A', '头等舱', 9, 'A', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (386, 12, '9B', '头等舱', 9, 'B', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (387, 12, '9C', '头等舱', 9, 'C', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (388, 12, '9D', '头等舱', 9, 'D', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (389, 12, '9E', '头等舱', 9, 'E', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (390, 12, '9F', '头等舱', 9, 'F', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (391, 12, '10A', '头等舱', 10, 'A', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (392, 12, '10B', '头等舱', 10, 'B', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (393, 12, '10C', '头等舱', 10, 'C', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (394, 12, '10D', '头等舱', 10, 'D', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (395, 12, '10E', '头等舱', 10, 'E', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (396, 12, '10F', '头等舱', 10, 'F', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (397, 12, '11A', '头等舱', 11, 'A', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (398, 12, '11B', '头等舱', 11, 'B', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (399, 12, '11C', '头等舱', 11, 'C', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (400, 12, '11D', '头等舱', 11, 'D', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (401, 12, '11E', '头等舱', 11, 'E', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (402, 12, '11F', '头等舱', 11, 'F', 405.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:20');
INSERT INTO `seats` VALUES (403, 12, '12A', '经济舱', 12, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (404, 12, '12B', '经济舱', 12, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (405, 12, '12C', '经济舱', 12, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (406, 12, '12D', '经济舱', 12, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (407, 12, '12E', '经济舱', 12, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (408, 12, '12F', '经济舱', 12, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (409, 12, '13A', '经济舱', 13, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (410, 12, '13B', '经济舱', 13, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (411, 12, '13C', '经济舱', 13, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (412, 12, '13D', '经济舱', 13, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (413, 12, '13E', '经济舱', 13, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (414, 12, '13F', '经济舱', 13, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (415, 12, '14A', '经济舱', 14, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (416, 12, '14B', '经济舱', 14, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (417, 12, '14C', '经济舱', 14, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (418, 12, '14D', '经济舱', 14, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (419, 12, '14E', '经济舱', 14, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (420, 12, '14F', '经济舱', 14, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (421, 12, '15A', '经济舱', 15, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (422, 12, '15B', '经济舱', 15, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (423, 12, '15C', '经济舱', 15, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (424, 12, '15D', '经济舱', 15, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (425, 12, '15E', '经济舱', 15, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (426, 12, '15F', '经济舱', 15, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (427, 12, '16A', '经济舱', 16, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (428, 12, '16B', '经济舱', 16, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (429, 12, '16C', '经济舱', 16, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (430, 12, '16D', '经济舱', 16, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (431, 12, '16E', '经济舱', 16, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (432, 12, '16F', '经济舱', 16, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (433, 12, '17A', '经济舱', 17, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (434, 12, '17B', '经济舱', 17, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (435, 12, '17C', '经济舱', 17, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (436, 12, '17D', '经济舱', 17, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (437, 12, '17E', '经济舱', 17, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (438, 12, '17F', '经济舱', 17, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (439, 12, '18A', '经济舱', 18, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (440, 12, '18B', '经济舱', 18, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (441, 12, '18C', '经济舱', 18, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (442, 12, '18D', '经济舱', 18, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (443, 12, '18E', '经济舱', 18, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (444, 12, '18F', '经济舱', 18, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (445, 12, '19A', '经济舱', 19, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (446, 12, '19B', '经济舱', 19, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (447, 12, '19C', '经济舱', 19, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (448, 12, '19D', '经济舱', 19, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (449, 12, '19E', '经济舱', 19, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (450, 12, '19F', '经济舱', 19, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (451, 12, '20A', '经济舱', 20, 'A', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (452, 12, '20B', '经济舱', 20, 'B', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (453, 12, '20C', '经济舱', 20, 'C', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (454, 12, '20D', '经济舱', 20, 'D', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (455, 12, '20E', '经济舱', 20, 'E', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (456, 12, '20F', '经济舱', 20, 'F', 205.00, 1, '可用', '2025-12-18 11:39:20', '2025-12-18 11:46:07');
INSERT INTO `seats` VALUES (458, 13, '1A', '商务舱', 1, 'A', 310.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:54');
INSERT INTO `seats` VALUES (459, 13, '1B', '商务舱', 1, 'B', 310.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:54');
INSERT INTO `seats` VALUES (460, 13, '1C', '商务舱', 1, 'C', 310.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:54');
INSERT INTO `seats` VALUES (461, 13, '1D', '商务舱', 1, 'D', 310.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:54');
INSERT INTO `seats` VALUES (462, 13, '2A', '商务舱', 2, 'A', 310.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:54');
INSERT INTO `seats` VALUES (463, 13, '2B', '商务舱', 2, 'B', 310.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:54');
INSERT INTO `seats` VALUES (464, 13, '2C', '商务舱', 2, 'C', 310.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:54');
INSERT INTO `seats` VALUES (465, 13, '2D', '商务舱', 2, 'D', 310.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:54');
INSERT INTO `seats` VALUES (466, 13, '3A', '头等舱', 3, 'A', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (467, 13, '3B', '头等舱', 3, 'B', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (468, 13, '3C', '头等舱', 3, 'C', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (469, 13, '3D', '头等舱', 3, 'D', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (470, 13, '3E', '头等舱', 3, 'E', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (471, 13, '3F', '头等舱', 3, 'F', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (472, 13, '4A', '头等舱', 4, 'A', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (473, 13, '4B', '头等舱', 4, 'B', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (474, 13, '4C', '头等舱', 4, 'C', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (475, 13, '4D', '头等舱', 4, 'D', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (476, 13, '4E', '头等舱', 4, 'E', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (477, 13, '4F', '头等舱', 4, 'F', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (478, 13, '5A', '头等舱', 5, 'A', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (479, 13, '5B', '头等舱', 5, 'B', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (480, 13, '5C', '头等舱', 5, 'C', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (481, 13, '5D', '头等舱', 5, 'D', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (482, 13, '5E', '头等舱', 5, 'E', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (483, 13, '5F', '头等舱', 5, 'F', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (484, 13, '6A', '头等舱', 6, 'A', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (485, 13, '6B', '头等舱', 6, 'B', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (486, 13, '6C', '头等舱', 6, 'C', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (487, 13, '6D', '头等舱', 6, 'D', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (488, 13, '6E', '头等舱', 6, 'E', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (489, 13, '6F', '头等舱', 6, 'F', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (490, 13, '7A', '头等舱', 7, 'A', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (491, 13, '7B', '头等舱', 7, 'B', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (492, 13, '7C', '头等舱', 7, 'C', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (493, 13, '7D', '头等舱', 7, 'D', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (494, 13, '7E', '头等舱', 7, 'E', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (495, 13, '7F', '头等舱', 7, 'F', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (496, 13, '8A', '头等舱', 8, 'A', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (497, 13, '8B', '头等舱', 8, 'B', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (498, 13, '8C', '头等舱', 8, 'C', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (499, 13, '8D', '头等舱', 8, 'D', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (500, 13, '8E', '头等舱', 8, 'E', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (501, 13, '8F', '头等舱', 8, 'F', 410.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:47:00');
INSERT INTO `seats` VALUES (502, 13, '9A', '经济舱', 9, 'A', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (503, 13, '9B', '经济舱', 9, 'B', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (504, 13, '9C', '经济舱', 9, 'C', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (505, 13, '9D', '经济舱', 9, 'D', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (506, 13, '9E', '经济舱', 9, 'E', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (507, 13, '9F', '经济舱', 9, 'F', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (508, 13, '10A', '经济舱', 10, 'A', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (509, 13, '10B', '经济舱', 10, 'B', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (510, 13, '10C', '经济舱', 10, 'C', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (511, 13, '10D', '经济舱', 10, 'D', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (512, 13, '10E', '经济舱', 10, 'E', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (513, 13, '10F', '经济舱', 10, 'F', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (514, 13, '11A', '经济舱', 11, 'A', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (515, 13, '11B', '经济舱', 11, 'B', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (516, 13, '11C', '经济舱', 11, 'C', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (517, 13, '11D', '经济舱', 11, 'D', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (518, 13, '11E', '经济舱', 11, 'E', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (519, 13, '11F', '经济舱', 11, 'F', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (520, 13, '12A', '经济舱', 12, 'A', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (521, 13, '12B', '经济舱', 12, 'B', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (522, 13, '12C', '经济舱', 12, 'C', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (523, 13, '12D', '经济舱', 12, 'D', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (524, 13, '12E', '经济舱', 12, 'E', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (525, 13, '12F', '经济舱', 12, 'F', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (526, 13, '13A', '经济舱', 13, 'A', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (527, 13, '13B', '经济舱', 13, 'B', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (528, 13, '13C', '经济舱', 13, 'C', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (529, 13, '13D', '经济舱', 13, 'D', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (530, 13, '13E', '经济舱', 13, 'E', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (531, 13, '13F', '经济舱', 13, 'F', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (532, 13, '14A', '经济舱', 14, 'A', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (533, 13, '14B', '经济舱', 14, 'B', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (534, 13, '14C', '经济舱', 14, 'C', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (535, 13, '14D', '经济舱', 14, 'D', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (536, 13, '14E', '经济舱', 14, 'E', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (537, 13, '14F', '经济舱', 14, 'F', 210.00, 1, '可用', '2025-12-18 11:39:36', '2025-12-18 11:46:48');
INSERT INTO `seats` VALUES (538, 15, '1A', '头等舱', 1, 'A', 420.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:10');
INSERT INTO `seats` VALUES (539, 15, '1B', '头等舱', 1, 'B', 420.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:10');
INSERT INTO `seats` VALUES (540, 15, '1C', '头等舱', 1, 'C', 420.00, 1, '已预订', '2025-12-18 11:42:00', '2025-12-18 11:48:10');
INSERT INTO `seats` VALUES (541, 15, '1D', '头等舱', 1, 'D', 420.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:10');
INSERT INTO `seats` VALUES (542, 15, '2A', '头等舱', 2, 'A', 420.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:10');
INSERT INTO `seats` VALUES (543, 15, '2B', '头等舱', 2, 'B', 420.00, 1, '已占用', '2025-12-18 11:42:00', '2025-12-18 11:48:10');
INSERT INTO `seats` VALUES (544, 15, '2C', '头等舱', 2, 'C', 420.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:10');
INSERT INTO `seats` VALUES (545, 15, '2D', '头等舱', 2, 'D', 420.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:10');
INSERT INTO `seats` VALUES (546, 15, '3A', '商务舱', 3, 'A', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (547, 15, '3B', '商务舱', 3, 'B', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (548, 15, '3C', '商务舱', 3, 'C', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (549, 15, '3D', '商务舱', 3, 'D', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (550, 15, '4A', '商务舱', 4, 'A', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (551, 15, '4B', '商务舱', 4, 'B', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (552, 15, '4C', '商务舱', 4, 'C', 320.00, 1, '已预订', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (553, 15, '4D', '商务舱', 4, 'D', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (554, 15, '5A', '商务舱', 5, 'A', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (555, 15, '5B', '商务舱', 5, 'B', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (556, 15, '5C', '商务舱', 5, 'C', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (557, 15, '5D', '商务舱', 5, 'D', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (558, 15, '6A', '商务舱', 6, 'A', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (559, 15, '6B', '商务舱', 6, 'B', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (560, 15, '6C', '商务舱', 6, 'C', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (561, 15, '6D', '商务舱', 6, 'D', 320.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:48:05');
INSERT INTO `seats` VALUES (562, 15, '7A', '经济舱', 7, 'A', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (563, 15, '7B', '经济舱', 7, 'B', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (564, 15, '7C', '经济舱', 7, 'C', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (565, 15, '7D', '经济舱', 7, 'D', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (566, 15, '8A', '经济舱', 8, 'A', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (567, 15, '8B', '经济舱', 8, 'B', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (568, 15, '8C', '经济舱', 8, 'C', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (569, 15, '8D', '经济舱', 8, 'D', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (570, 15, '9A', '经济舱', 9, 'A', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (571, 15, '9B', '经济舱', 9, 'B', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (572, 15, '9C', '经济舱', 9, 'C', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (573, 15, '9D', '经济舱', 9, 'D', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (574, 15, '10A', '经济舱', 10, 'A', 220.00, 1, '已占用', '2025-12-18 11:42:00', '2025-12-25 18:01:55');
INSERT INTO `seats` VALUES (575, 15, '10B', '经济舱', 10, 'B', 220.00, 1, '已占用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (576, 15, '10C', '经济舱', 10, 'C', 220.00, 1, '已占用', '2025-12-18 11:42:00', '2025-12-25 18:36:14');
INSERT INTO `seats` VALUES (577, 15, '10D', '经济舱', 10, 'D', 220.00, 1, '已占用', '2025-12-18 11:42:00', '2025-12-25 18:38:27');
INSERT INTO `seats` VALUES (578, 15, '11A', '经济舱', 11, 'A', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (579, 15, '11B', '经济舱', 11, 'B', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (580, 15, '11C', '经济舱', 11, 'C', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (581, 15, '11D', '经济舱', 11, 'D', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (582, 15, '12A', '经济舱', 12, 'A', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (583, 15, '12B', '经济舱', 12, 'B', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (584, 15, '12C', '经济舱', 12, 'C', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (585, 15, '12D', '经济舱', 12, 'D', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (586, 15, '13A', '经济舱', 13, 'A', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (587, 15, '13B', '经济舱', 13, 'B', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (588, 15, '13C', '经济舱', 13, 'C', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (589, 15, '13D', '经济舱', 13, 'D', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (590, 15, '14A', '经济舱', 14, 'A', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (591, 15, '14B', '经济舱', 14, 'B', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (592, 15, '14C', '经济舱', 14, 'C', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (593, 15, '14D', '经济舱', 14, 'D', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (594, 15, '15A', '经济舱', 15, 'A', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (595, 15, '15B', '经济舱', 15, 'B', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (596, 15, '15C', '经济舱', 15, 'C', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (597, 15, '15D', '经济舱', 15, 'D', 220.00, 1, '可用', '2025-12-18 11:42:00', '2025-12-18 11:47:59');
INSERT INTO `seats` VALUES (599, 1131, '1A', 'business', 1, 'A', 800.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (600, 1131, '1B', 'business', 1, 'B', 800.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (601, 1131, '10A', 'economy', 10, 'A', 420.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (602, 1131, '10B', 'economy', 10, 'B', 420.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (603, 1131, '10C', 'economy', 10, 'C', 420.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (604, 1131, '10D', 'economy', 10, 'D', 420.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (605, 1132, '1A', 'business', 1, 'A', 900.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (606, 1132, '1B', 'business', 1, 'B', 900.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (607, 1132, '12A', 'economy', 12, 'A', 480.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (608, 1132, '12B', 'economy', 12, 'B', 480.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (609, 1132, '12C', 'economy', 12, 'C', 480.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (610, 1132, '12D', 'economy', 12, 'D', 480.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (611, 1133, '1A', 'business', 1, 'A', 700.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (612, 1133, '1B', 'business', 1, 'B', 700.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (613, 1133, '15A', 'economy', 15, 'A', 320.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (614, 1133, '15B', 'economy', 15, 'B', 320.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (615, 1133, '15C', 'economy', 15, 'C', 320.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (616, 1133, '15D', 'economy', 15, 'D', 320.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (617, 1134, '1A', 'business', 1, 'A', 1100.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (618, 1134, '1B', 'business', 1, 'B', 1100.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (619, 1134, '20A', 'economy', 20, 'A', 550.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (620, 1134, '20B', 'economy', 20, 'B', 550.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (621, 1134, '20C', 'economy', 20, 'C', 550.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (622, 1134, '20D', 'economy', 20, 'D', 550.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (623, 1135, '1A', 'business', 1, 'A', 760.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (624, 1135, '1B', 'business', 1, 'B', 760.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (625, 1135, '18A', 'economy', 18, 'A', 390.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (626, 1135, '18B', 'economy', 18, 'B', 390.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (627, 1135, '18C', 'economy', 18, 'C', 390.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `seats` VALUES (628, 1135, '18D', 'economy', 18, 'D', 390.00, 1, 'available', '2025-12-26 22:12:10', '2025-12-26 22:12:10');

-- ----------------------------
-- Table structure for special_service_requests
-- ----------------------------
DROP TABLE IF EXISTS `special_service_requests`;
CREATE TABLE `special_service_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `passenger_id` int NOT NULL COMMENT '乘客ID（关联users表，实现数据隔离）',
  `order_no` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `passenger_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '旅客类型（无陪伴年长旅客、无陪伴孕妇旅客、视觉障碍旅客、听觉障碍旅客、轮椅行动障碍旅客、担架行动障碍旅客、携带导盲犬旅客等）',
  `departure_airport` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出发机场',
  `arrival_airport` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '到达机场',
  `entry_services` json NULL COMMENT '进站服务需求（自备器械、优先进站、提供轮椅、提供担架等）',
  `exit_services` json NULL COMMENT '出站服务需求（自备器械、便利出站、提供轮椅、提供担架等）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '情况描述（非必填）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '待处理' COMMENT '申请状态（pending-待处理、approved-已批准、processing-处理中、completed-已完成、rejected-已拒绝、cancelled-已取消）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_passenger`(`passenger_id` ASC) USING BTREE COMMENT '乘客ID索引（用于快速查询该用户的预约）',
  INDEX `idx_order`(`order_no` ASC) USING BTREE COMMENT '订单ID索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态索引（用于快速查询不同状态的申请）',
  INDEX `idx_passenger_status`(`passenger_id` ASC, `status` ASC) USING BTREE COMMENT '乘客ID和状态联合索引',
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE COMMENT '创建时间索引',
  CONSTRAINT `fk_ssr_user` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '重点旅客预约表（乘客隔离，包含常用乘客关联）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of special_service_requests
-- ----------------------------
INSERT INTO `special_service_requests` VALUES (1, 3, 'ORD202511281600009', '15973773543', 'elderly', '西安咸阳国际机场', '昆明长水国际机场', '{\"stretcher\": false, \"wheelchair\": false, \"priorityEntry\": false, \"selfEquipment\": true}', '{\"stretcher\": false, \"wheelchair\": false, \"selfEquipment\": true, \"convenientExit\": false}', '你好', 'completed', '2025-12-14 20:41:24', '2026-01-04 17:16:44');
INSERT INTO `special_service_requests` VALUES (2, 3, 'ORD202511281600009', '15973773367', 'pregnant', '西安咸阳国际机场', '昆明长水国际机场', '{\"stretcher\": false, \"wheelchair\": false, \"priorityEntry\": false, \"selfEquipment\": true}', '{\"stretcher\": false, \"wheelchair\": false, \"selfEquipment\": true, \"convenientExit\": false}', '力哈', 'completed', '2025-12-14 20:51:10', '2026-01-04 17:20:36');
INSERT INTO `special_service_requests` VALUES (3, 3, 'ORD202511281600009', '15973773368', 'visual', '西安咸阳国际机场', '昆明长水国际机场', '{\"stretcher\": false, \"wheelchair\": false, \"priorityEntry\": true, \"selfEquipment\": false}', '{\"stretcher\": false, \"wheelchair\": false, \"selfEquipment\": true, \"convenientExit\": false}', '李好呀', 'processing', '2025-12-14 20:56:08', '2026-01-03 21:53:46');
INSERT INTO `special_service_requests` VALUES (4, 3, 'ORD20251217114209853', '15828824107', 'pregnant', '北京首都国际机场', '上海虹桥国际机场', '{\"stretcher\": false, \"wheelchair\": false, \"priorityEntry\": true, \"selfEquipment\": false}', '{\"stretcher\": false, \"wheelchair\": false, \"selfEquipment\": false, \"convenientExit\": true}', '11111', 'approved', '2025-12-19 08:32:23', '2026-01-04 15:12:24');
INSERT INTO `special_service_requests` VALUES (5, 3, '2025122422001426530508089548', '15973773399', 'elderly', '北京首都国际机场', '上海虹桥国际机场', '{\"stretcher\": false, \"wheelchair\": false, \"priorityEntry\": false, \"selfEquipment\": true}', '{\"stretcher\": false, \"wheelchair\": false, \"selfEquipment\": true, \"convenientExit\": false}', '11111', 'approved', '2025-12-24 12:25:19', '2026-01-04 15:12:58');
INSERT INTO `special_service_requests` VALUES (6, 3, '2025122522001426530508111952', '15828824107', 'guide_dog', '北京首都国际机场', '上海虹桥国际机场', '{\"stretcher\": false, \"wheelchair\": false, \"priorityEntry\": true, \"selfEquipment\": false}', '{\"stretcher\": false, \"wheelchair\": false, \"selfEquipment\": false, \"convenientExit\": true}', '111', 'rejected', '2025-12-25 19:12:43', '2026-01-04 15:13:09');
INSERT INTO `special_service_requests` VALUES (7, 3, '2025122422001426530508101431', '15828824107', 'elderly', '北京首都国际机场', '上海虹桥国际机场', '{\"stretcher\": false, \"wheelchair\": false, \"priorityEntry\": false, \"selfEquipment\": true}', '{\"stretcher\": false, \"wheelchair\": false, \"selfEquipment\": true, \"convenientExit\": true}', '13', 'processing', '2025-12-28 21:05:49', '2026-01-04 15:13:22');

-- ----------------------------
-- Table structure for ticket_cancel_requests
-- ----------------------------
DROP TABLE IF EXISTS `ticket_cancel_requests`;
CREATE TABLE `ticket_cancel_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `cancel_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '取消编号/申请单号，用于前端展示',
  `orderno` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `passenger_id` int NOT NULL COMMENT '用户ID/申请人，关联users(id)',
  `applicant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请人姓名（冗余展示）',
  `request_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `ticket_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '机票号（展示用）',
  `flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `route` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `departure_time` datetime NOT NULL,
  `cancel_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '取消费',
  `refund_fare` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '退款',
  `status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款状态',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '申请原因',
  `processed_by` int NULL DEFAULT NULL COMMENT '运营处理人ID，关联users(id)',
  `processed_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '运营备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_change_no`(`cancel_no` ASC) USING BTREE,
  INDEX `idx_order`(`orderno` ASC) USING BTREE,
  INDEX `idx_passenger_status`(`passenger_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_request_time`(`request_time` ASC) USING BTREE,
  INDEX `idx_new_dep`(`departure_time` ASC) USING BTREE,
  INDEX `fk_tcr_processeds`(`processed_by` ASC) USING BTREE,
  CONSTRAINT `fk_tcr_orders` FOREIGN KEY (`orderno`) REFERENCES `orders` (`order_no`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_tcr_passengers` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_tcr_processeds` FOREIGN KEY (`processed_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '取消申请' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ticket_cancel_requests
-- ----------------------------
INSERT INTO `ticket_cancel_requests` VALUES (2, 'TCA20251222214017965', 'ORD202512221123477849', 3, '普通乘客', '2025-12-22 21:40:17', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 0.05, 0.95, '通过', '111', 2, '2025-12-24 22:25:18', NULL);
INSERT INTO `ticket_cancel_requests` VALUES (3, 'TCA20251222214322942', 'ORD202512221951526981', 3, '普通乘客', '2025-12-22 21:43:23', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 7.55, 143.45, '通过', '11', 2, '2025-12-24 23:30:04', NULL);
INSERT INTO `ticket_cancel_requests` VALUES (4, 'TCA20251222214519583', 'ORD202512221948189133', 3, '普通乘客', '2025-12-22 21:45:20', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 0.05, 0.95, '拒绝', '111', 2, '2025-12-24 23:39:29', '12');
INSERT INTO `ticket_cancel_requests` VALUES (5, 'TCA20251222214614759', 'ORD202512221930197043', 3, '普通乘客', '2025-12-22 21:46:14', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 0.05, 0.95, '不通过', '11', 2, '2025-12-25 10:26:40', '1111313141');
INSERT INTO `ticket_cancel_requests` VALUES (6, 'TCA20251223103809790', 'ORD202512212132594334', 3, '普通乘客', '2025-12-23 10:38:09', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 10.05, 190.95, '不通过', '111', 2, '2025-12-25 10:34:59', '123456');
INSERT INTO `ticket_cancel_requests` VALUES (7, 'CNL202512231139591211', 'ORD202512211131555077', 3, '李好呀呀呀', '2025-12-23 11:39:59', 'TKT20251221113155865683', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 0.05, 0.95, '不通过', '12', 1, '2025-12-25 11:53:34', '111111');
INSERT INTO `ticket_cancel_requests` VALUES (8, 'CNL202512231154572293', 'ORD202512211129204244', 3, '普通乘客', '2025-12-23 11:54:57', 'TKT20251221112920743498', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 0.05, 0.95, '通过', '12', 1, '2026-01-04 15:10:29', NULL);
INSERT INTO `ticket_cancel_requests` VALUES (9, 'CNL202512231156445902', 'ORD202512211123462144', 3, '普通乘客', '2025-12-23 11:56:44', 'TKT20251221112346334462', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 0.05, 0.95, '通过', '12', 1, '2026-01-04 15:10:17', NULL);
INSERT INTO `ticket_cancel_requests` VALUES (10, 'CNL202512231321265193', 'ORD202512211111417363', 3, '普通乘客', '2025-12-23 13:21:26', 'TKT20251221111141414645', 'MU5432', '上海浦东国际机场 → 成都双流国际机场', '2025-12-09 10:00:00', 42.50, 807.50, '不通过', '5', 1, '2026-01-04 15:10:41', '1111');
INSERT INTO `ticket_cancel_requests` VALUES (12, 'TCA20251223163951242', 'ORD202512231620312834', 3, '普通乘客', '2025-12-23 16:39:51', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 10.05, 190.95, '通过', '1', 2, '2026-01-04 17:13:18', NULL);
INSERT INTO `ticket_cancel_requests` VALUES (13, 'TCA20251223171225849', '2025122322001426530508094572', 3, '普通乘客', '2025-12-23 17:12:26', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 10.05, 190.95, '不通过', '12', 2, '2026-01-04 17:13:34', '1111');
INSERT INTO `ticket_cancel_requests` VALUES (14, 'TCA20251223184043891', 'ORD202512231800371558', 3, '普通乘客', '2025-12-23 18:40:43', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 10.05, 190.95, '待审核', '12', NULL, NULL, NULL);
INSERT INTO `ticket_cancel_requests` VALUES (15, 'TCA20251223184804853', 'ORD202512231658168232', 3, '普通乘客', '2025-12-23 18:48:04', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 10.05, 190.95, '待审核', '12', NULL, NULL, NULL);
INSERT INTO `ticket_cancel_requests` VALUES (16, 'TCA20251223185146388', 'ORD202512231759154459', 3, '普通乘客', '2025-12-23 18:51:47', NULL, 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 10.05, 190.95, '待审核', '12', NULL, NULL, NULL);
INSERT INTO `ticket_cancel_requests` VALUES (17, 'TCA20251223185611370', 'ORD202512231619343432', 3, '普通乘客', '2025-12-23 18:56:12', 'TKT20251223161934823526', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 10.05, 190.95, '待审核', '12', NULL, NULL, NULL);
INSERT INTO `ticket_cancel_requests` VALUES (18, 'TCA20251224123511543', '2025122422001426530508089548', 3, '普通乘客', '2025-12-24 12:35:11', 'TKT20251224103820191450', 'HU7615', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', 50.18, 953.32, '待审核', '12', NULL, NULL, NULL);
INSERT INTO `ticket_cancel_requests` VALUES (19, 'TCA20251225184352101', 'ORD20251225183950527', 3, '普通乘客', '2025-12-25 18:43:53', 'TKT20251225183950841370', 'HO1234', '上海虹桥国际机场 → 成都天府国际机场', '2025-12-22 16:00:00', 61.00, 1159.00, '待审核', '12', NULL, NULL, NULL);
INSERT INTO `ticket_cancel_requests` VALUES (20, 'TCA20251228211352550', '2025122422001426530508101430', 3, '普通乘客', '2025-12-28 21:13:53', 'TKT20251224130157221442', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 7.30, 138.70, '待审核', '12', NULL, NULL, NULL);
INSERT INTO `ticket_cancel_requests` VALUES (21, 'TCA20260103134027407', '2026010322001426530508203237', 3, '普通乘客', '2026-01-03 13:40:28', 'TKT20260103113704209041', 'CA1501', 'PEK → SHA', '2026-01-07 08:30:00', 236.50, 2128.50, '通过', '不想去', 1, '2026-01-03 13:42:05', NULL);

-- ----------------------------
-- Table structure for ticket_change_requests
-- ----------------------------
DROP TABLE IF EXISTS `ticket_change_requests`;
CREATE TABLE `ticket_change_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `change_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '改签编号/申请单号，用于前端展示',
  `orderno` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号（冗余展示，来自orders.order_no）',
  `passenger_id` int NOT NULL COMMENT '用户ID/申请人，关联users(id)',
  `applicant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '申请人姓名（冗余展示）',
  `request_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `ticket_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '机票号（展示用）',
  `old_flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '旧航班号（字符）',
  `old_route` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '旧航线，例：北京 → 上海',
  `old_departure_time` datetime NOT NULL COMMENT '旧航线起飞时间',
  `new_flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新航班号（字符）',
  `new_route` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '新航线',
  `new_departure_time` datetime NOT NULL COMMENT '新的起飞时间',
  `change_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '改签费',
  `fare_diff` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '差价（正数需补、负数应退）',
  `status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '改签状态',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '申请原因',
  `processed_by` int NULL DEFAULT NULL COMMENT '运营处理人ID，关联users(id)',
  `processed_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '运营备注',
  `new_order_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '新订单号（改签生成的新订单）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_change_no`(`change_no` ASC) USING BTREE,
  INDEX `idx_order`(`orderno` ASC) USING BTREE,
  INDEX `idx_passenger_status`(`passenger_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_request_time`(`request_time` ASC) USING BTREE,
  INDEX `idx_old_dep`(`old_departure_time` ASC) USING BTREE,
  INDEX `idx_new_dep`(`new_departure_time` ASC) USING BTREE,
  INDEX `fk_tcr_processed`(`processed_by` ASC) USING BTREE,
  INDEX `fk_tcr_new_order`(`new_order_no` ASC) USING BTREE,
  CONSTRAINT `fk_tcr_new_order` FOREIGN KEY (`new_order_no`) REFERENCES `orders` (`order_no`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_tcr_order` FOREIGN KEY (`orderno`) REFERENCES `orders` (`order_no`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_tcr_passenger` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_tcr_processed` FOREIGN KEY (`processed_by`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '改签申请（乘客与运营共用，含旧/新航班、费用、状态等）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ticket_change_requests
-- ----------------------------
INSERT INTO `ticket_change_requests` VALUES (11, 'TCR202512241346471895', '2025122422001426530508101431', 3, '普通乘客', '2025-12-24 13:46:47', 'TKT20251224132641488264', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-24 13:46:47', 7.55, 0.00, '待审核', '1112', 1, '2025-12-25 11:32:34', '123456', 'ORD20251224134647972');
INSERT INTO `ticket_change_requests` VALUES (12, 'TCR202512241354385346', '2025122422001426530508099765', 3, '普通乘客', '2025-12-24 13:54:38', 'TKT20251224132502411690', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 7.55, 0.00, '不通过', '1', 2, '2025-12-24 23:42:19', '12', 'ORD20251224135438198');
INSERT INTO `ticket_change_requests` VALUES (14, 'TCR202512251839508913', '2025122522001426530508113155', 3, '普通乘客', '2025-12-25 18:39:50', 'TKT20251225180233802651', 'HO1234', '上海虹桥国际机场 → 成都天府国际机场', '2025-12-22 16:00:00', 'HO1234', '上海虹桥国际机场 → 成都天府国际机场', '2025-12-22 16:00:00', 61.00, 0.00, '通过', '12', 2, '2025-12-25 18:41:02', NULL, 'ORD20251225183950527');
INSERT INTO `ticket_change_requests` VALUES (15, 'TCR202512251922598904', '2025122522001426530508111952', 3, '普通乘客', '2025-12-25 19:22:59', 'TKT20251225175302094541', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 7.55, 0.00, '通过', '123', 2, '2025-12-25 19:24:27', NULL, 'ORD20251225192259863');
INSERT INTO `ticket_change_requests` VALUES (16, 'TCR202512260018072137', 'ORD20251225192259863', 3, '普通乘客', '2025-12-26 00:18:07', 'TKT20251225192259099157', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 7.55, 0.00, '待审核', '5656', 2, '2026-01-04 17:12:58', '1111', 'ORD20251226001807354');
INSERT INTO `ticket_change_requests` VALUES (17, 'TCR202512272244063613', '2025122722001426530508142492', 3, '普通乘客', '2025-12-27 22:44:06', 'TKT20251227223511070313', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 10.05, 0.00, '通过', '2', 2, '2025-12-27 22:45:03', NULL, 'ORD20251227224406449');
INSERT INTO `ticket_change_requests` VALUES (18, 'TCR202512282115363740', 'ORD20251227224406449', 3, '普通乘客', '2025-12-28 21:15:36', 'TKT20251227224406865767', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', 'HU7615', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', 10.05, 0.00, '通过', '222', 2, '2025-12-28 21:17:31', NULL, 'ORD20251228211536486');
INSERT INTO `ticket_change_requests` VALUES (19, 'TCR202512291717521216', 'ORD20251224102409234', 3, '普通乘客', '2025-12-29 17:17:52', 'TKT20251224102409002753', 'CA1201', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-24 10:24:10', 'HU7615', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', 0.05, 0.00, '通过', '1313131212', 2, '2026-01-04 17:12:43', NULL, 'ORD20251229171752243');
INSERT INTO `ticket_change_requests` VALUES (20, 'TCR202512301006219061', 'ORD20251228211536486', 3, '普通乘客', '2025-12-30 10:06:21', 'TKT20251228211536945213', 'HU7615', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', 'HU7615', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', 10.05, 0.00, '通过', '111', 1, '2026-01-04 15:08:35', NULL, 'ORD20251230100621191');
INSERT INTO `ticket_change_requests` VALUES (21, 'TCR202601031340032185', '2026010322001426530508203238', 3, '普通乘客', '2026-01-03 13:40:03', 'TKT20260103113815051136', 'CA1501', 'PEK → SHA', '2026-01-07 08:30:00', 'CA1519', 'PEK → SHA', '2026-01-07 09:30:00', 236.50, -215.00, '通过', '赶不到机场', 2, '2026-01-03 13:40:59', NULL, 'ORD20260103134003624');

-- ----------------------------
-- Table structure for tickets
-- ----------------------------
DROP TABLE IF EXISTS `tickets`;
CREATE TABLE `tickets`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '机票ID',
  `order_no` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号，关联orders(order_no)，冗余字段便于查询',
  `passenger_id` int NOT NULL COMMENT '乘客ID（关联users表，实现数据隔离）',
  `passenger_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '乘客姓名（冗余字段，便于查询）',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证号',
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `flight_id` int NOT NULL COMMENT '航班ID，关联flights(id)',
  `flight_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '航班号',
  `origin_airport` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出发机场',
  `dest_airport` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '到达机场',
  `route` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '航线（格式：出发地 → 目的地，如：北京 → 上海）',
  `departure_time` datetime NOT NULL COMMENT '起飞时间',
  `arrival_time` datetime NOT NULL COMMENT '到达时间',
  `seat_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '座位号，如：10A, 12B等',
  `seat_class` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '舱位等级（economy-经济舱, business-商务舱, first-头等舱）',
  `base_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '基础票价',
  `seat_fee` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '座位选择费',
  `discount_fee` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠费（如有）',
  `total_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '机票总价（基础票价 + 座位费）',
  `ticket_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机票号（唯一标识，格式：如TKT+时间戳+随机数）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机票状态',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ticket_no`(`ticket_no` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_passenger_id`(`passenger_id` ASC) USING BTREE,
  INDEX `idx_passenger_name`(`passenger_name` ASC) USING BTREE,
  INDEX `idx_id_card`(`id_card` ASC) USING BTREE,
  INDEX `idx_flight_id`(`flight_id` ASC) USING BTREE,
  INDEX `idx_flight_no`(`flight_no` ASC) USING BTREE,
  INDEX `idx_departure_time`(`departure_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_ticket_no`(`ticket_no` ASC) USING BTREE,
  INDEX `idx_passenger_flight`(`passenger_id` ASC, `flight_id` ASC) USING BTREE,
  INDEX `idx_order_passenger`(`order_no` ASC, `passenger_id` ASC) USING BTREE,
  CONSTRAINT `fk_ticket_flight` FOREIGN KEY (`flight_id`) REFERENCES `flights` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ticket_order` FOREIGN KEY (`order_no`) REFERENCES `orders` (`order_no`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_ticket_user` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 97 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '机票表（每个乘客一张机票，支持一个订单多张机票）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tickets
-- ----------------------------
INSERT INTO `tickets` VALUES (2, 'ORD202512201632440890', 3, '张浩然', '110101201512036215', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '10C', '经济舱', 800.00, 200.00, 0.00, 1000.00, 'TKT20251220163244711841', '已完成', '2025-12-20 16:32:44', '2025-12-25 18:09:33');
INSERT INTO `tickets` VALUES (4, 'ORD202512201845579095', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '10E', '经济舱', 800.00, 200.00, 0.00, 1000.00, 'TKT20251220184557118324', 'deleted', '2025-12-20 18:45:57', '2025-12-25 18:08:50');
INSERT INTO `tickets` VALUES (5, 'ORD202512201847316460', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '10F', '经济舱', 800.00, 200.00, 50.00, 950.00, 'TKT20251220184731107865', 'deleted', '2025-12-20 18:47:31', '2025-12-25 19:04:12');
INSERT INTO `tickets` VALUES (6, 'ORD202512201848452096', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 800.00, 0.00, 50.00, 750.00, 'TKT20251220184845757424', '已出票', '2025-12-20 18:48:46', '2025-12-20 18:48:46');
INSERT INTO `tickets` VALUES (7, 'ORD202512201850436585', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '11A', '经济舱', 800.00, 200.00, 50.00, 950.00, 'TKT20251220185043056016', '已出票', '2025-12-20 18:50:44', '2025-12-20 18:50:44');
INSERT INTO `tickets` VALUES (8, 'ORD202512201904097659', 3, '张浩然', '110101201512036215', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '11B', '经济舱', 800.00, 200.00, 50.00, 950.00, 'TKT20251220190409905491', '已出票', '2025-12-20 19:04:10', '2025-12-20 19:04:10');
INSERT INTO `tickets` VALUES (9, 'ORD202512202054064543', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 800.00, 0.00, 0.00, 800.00, 'TKT20251220205406064693', '已出票', '2025-12-20 20:54:07', '2025-12-20 20:54:07');
INSERT INTO `tickets` VALUES (10, 'ORD202512202059140721', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 800.00, 0.00, 0.00, 800.00, 'TKT20251220205914191442', '已出票', '2025-12-20 20:59:14', '2025-12-20 20:59:14');
INSERT INTO `tickets` VALUES (11, 'ORD202512202100358421', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 800.00, 0.00, 0.00, 800.00, 'TKT20251220210035990606', '已出票', '2025-12-20 21:00:35', '2025-12-20 21:00:35');
INSERT INTO `tickets` VALUES (12, 'ORD202512202135182004', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220213518381726', '已出票', '2025-12-20 21:35:19', '2025-12-20 21:35:19');
INSERT INTO `tickets` VALUES (13, 'ORD202512202159206482', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220215920915340', '已出票', '2025-12-20 21:59:20', '2025-12-20 21:59:20');
INSERT INTO `tickets` VALUES (14, 'ORD202512202234402053', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220223440784657', '已出票', '2025-12-20 22:34:41', '2025-12-20 22:34:41');
INSERT INTO `tickets` VALUES (15, 'ORD202512202241000219', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220224100982431', '已出票', '2025-12-20 22:41:01', '2025-12-20 22:41:01');
INSERT INTO `tickets` VALUES (16, 'ORD202512202248372909', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220224837908096', '已出票', '2025-12-20 22:48:37', '2025-12-20 22:48:37');
INSERT INTO `tickets` VALUES (17, 'ORD202512202257077856', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220225707345210', '已出票', '2025-12-20 22:57:07', '2025-12-20 22:57:07');
INSERT INTO `tickets` VALUES (18, 'ORD202512202258572326', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220225857068278', '已出票', '2025-12-20 22:58:58', '2025-12-20 22:58:58');
INSERT INTO `tickets` VALUES (19, 'ORD202512202302127976', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220230212515904', '已出票', '2025-12-20 23:02:12', '2025-12-20 23:02:12');
INSERT INTO `tickets` VALUES (20, 'ORD202512202306480987', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220230648999521', '已出票', '2025-12-20 23:06:48', '2025-12-20 23:06:48');
INSERT INTO `tickets` VALUES (21, 'ORD202512202309428473', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220230942706883', '已出票', '2025-12-20 23:09:43', '2025-12-20 23:09:43');
INSERT INTO `tickets` VALUES (22, 'ORD202512202314366434', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220231436188945', '已出票', '2025-12-20 23:14:36', '2025-12-20 23:14:36');
INSERT INTO `tickets` VALUES (23, 'ORD202512202315058025', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220231505341314', '已出票', '2025-12-20 23:15:05', '2025-12-20 23:15:05');
INSERT INTO `tickets` VALUES (24, 'ORD202512202316218456', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220231621859189', '已出票', '2025-12-20 23:16:22', '2025-12-20 23:16:22');
INSERT INTO `tickets` VALUES (25, 'ORD202512202317089477', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251220231708727305', '已出票', '2025-12-20 23:17:09', '2025-12-20 23:17:09');
INSERT INTO `tickets` VALUES (27, 'ORD202512211024067914', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221102406359302', '已出票', '2025-12-21 10:24:07', '2025-12-21 10:24:07');
INSERT INTO `tickets` VALUES (28, 'ORD202512211024288847', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221102428005051', '已出票', '2025-12-21 10:24:28', '2025-12-21 10:24:28');
INSERT INTO `tickets` VALUES (29, 'ORD202512211025362820', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221102536633348', '待处理', '2025-12-21 10:25:37', '2025-12-23 20:42:50');
INSERT INTO `tickets` VALUES (30, 'ORD202512211026597704', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221102659687365', '待处理', '2025-12-21 10:26:59', '2025-12-23 20:30:17');
INSERT INTO `tickets` VALUES (31, 'ORD202512211028298071', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221102829796042', '待处理', '2025-12-21 10:28:29', '2025-12-23 20:08:34');
INSERT INTO `tickets` VALUES (33, 'ORD202512211035063881', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221103506631946', '待审核', '2025-12-21 10:35:06', '2025-12-23 19:47:38');
INSERT INTO `tickets` VALUES (36, 'ORD202512211111417363', 3, '张建', '110101196505128976', '13600136000', 12, 'MU5432', '上海浦东国际机场', '成都双流国际机场', '上海浦东国际机场 → 成都双流国际机场', '2025-12-09 10:00:00', '2025-12-09 14:00:00', NULL, '经济舱', 850.00, 0.00, 0.00, 850.00, 'TKT20251221111141414645', '已出票', '2025-12-21 11:11:41', '2026-01-04 15:10:41');
INSERT INTO `tickets` VALUES (37, 'ORD202512211123462144', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221112346334462', '待退款', '2025-12-21 11:23:47', '2026-01-04 15:10:17');
INSERT INTO `tickets` VALUES (38, 'ORD202512211129204244', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221112920743498', '待退款', '2025-12-21 11:29:20', '2026-01-04 15:10:29');
INSERT INTO `tickets` VALUES (39, 'ORD202512211131555077', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221113155865683', '已出票', '2025-12-21 11:31:56', '2025-12-25 11:53:34');
INSERT INTO `tickets` VALUES (40, 'ORD202512211337188162', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251221133718100159', '待审核', '2025-12-21 13:37:18', '2025-12-23 11:33:58');
INSERT INTO `tickets` VALUES (41, 'ORD202512211406237817', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '11C', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251221140623352436', '待审核', '2025-12-21 14:06:23', '2025-12-23 11:28:01');
INSERT INTO `tickets` VALUES (42, 'ORD202512211700461628', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '11D', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251221170046970917', '待审核', '2025-12-21 17:00:46', '2025-12-23 11:21:08');
INSERT INTO `tickets` VALUES (43, 'ORD202512212130123567', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '11E', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251221213012392339', '待审核', '2025-12-21 21:30:12', '2025-12-23 10:51:42');
INSERT INTO `tickets` VALUES (44, 'ORD202512212132594334', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '11F', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251221213259488235', '已出票', '2025-12-21 21:33:00', '2025-12-21 21:33:00');
INSERT INTO `tickets` VALUES (45, 'ORD202512221123477849', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251222112347297809', '待退款', '2025-12-22 11:23:47', '2025-12-24 22:25:19');
INSERT INTO `tickets` VALUES (46, 'ORD202512221752034043', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '12A', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251222175203479338', '待审核', '2025-12-22 17:52:03', '2025-12-23 10:45:51');
INSERT INTO `tickets` VALUES (47, 'ORD202512221930197043', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251222193019750131', '待出行', '2025-12-22 19:30:19', '2025-12-25 10:26:40');
INSERT INTO `tickets` VALUES (48, 'ORD202512221948189133', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251222194818634023', '待出行', '2025-12-22 19:48:18', '2025-12-24 23:39:29');
INSERT INTO `tickets` VALUES (49, 'ORD202512221951526981', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '12B', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251222195152072413', '待退款', '2025-12-22 19:51:52', '2025-12-24 23:30:04');
INSERT INTO `tickets` VALUES (50, 'ORD202512231049251434', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '12E', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223104925048985', '待审核', '2025-12-23 10:49:25', '2025-12-23 11:14:07');
INSERT INTO `tickets` VALUES (51, 'ORD202512231607146768', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '13A', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223160714493467', '待审核', '2025-12-23 16:07:15', '2025-12-23 19:18:41');
INSERT INTO `tickets` VALUES (52, 'ORD202512231619343432', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '13B', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223161934823526', '待审核', '2025-12-23 16:19:35', '2025-12-23 18:56:12');
INSERT INTO `tickets` VALUES (53, 'ORD202512231620312834', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '13C', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223162031502362', '待退款', '2025-12-23 16:20:32', '2026-01-04 17:13:18');
INSERT INTO `tickets` VALUES (54, 'ORD202512231658168232', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '13E', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223165816475370', '待审核', '2025-12-23 16:58:16', '2025-12-23 18:48:04');
INSERT INTO `tickets` VALUES (55, '2025122322001426530508094572', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '13F', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223171120542202', '已出票', '2025-12-23 17:11:21', '2026-01-04 17:13:34');
INSERT INTO `tickets` VALUES (56, '2025122322001426530508096488', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '14A', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223173943905170', '已完成', '2025-12-23 17:39:44', '2025-12-23 19:15:51');
INSERT INTO `tickets` VALUES (57, 'ORD202512231759154459', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '14B', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223175915947651', '待审核', '2025-12-23 17:59:16', '2025-12-23 18:51:47');
INSERT INTO `tickets` VALUES (58, 'ORD202512231800371558', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '14C', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223180037551430', '待审核', '2025-12-23 18:00:38', '2025-12-23 18:40:43');
INSERT INTO `tickets` VALUES (59, 'ORD20251223200833891', 3, '李好呀呀呀', '123456789987654344', NULL, 14, 'HU7615', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-23 20:08:34', '2025-12-09 12:00:00', NULL, '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251223200833894753', '待审核', '2025-12-23 20:08:34', '2025-12-23 20:08:34');
INSERT INTO `tickets` VALUES (61, 'ORD20251223204249253', 3, '张建', '110101196505128976', '15973773356', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-23 20:42:50', '2025-12-09 12:00:00', '15B', '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251223204249334105', '待处理', '2025-12-23 20:42:50', '2025-12-23 22:46:31');
INSERT INTO `tickets` VALUES (62, '2025122322001426530508088246', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '15E', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223210032844280', '待处理', '2025-12-23 21:00:32', '2025-12-23 21:14:50');
INSERT INTO `tickets` VALUES (63, 'ORD20251223211450788', 3, '李好呀呀呀', '123456789987654344', '15973773356', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-23 21:14:50', '2025-12-09 12:00:00', '16A', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223211450060646', '待审核', '2025-12-23 21:14:50', '2025-12-23 21:14:50');
INSERT INTO `tickets` VALUES (64, '2025122322001426530508089546', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '16B', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223211640895694', '已出票', '2025-12-23 21:16:40', '2025-12-23 21:16:40');
INSERT INTO `tickets` VALUES (65, '2025122322001426530508091002', 3, '李好呀呀呀', '123456789987654344', '15973773354', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '16C', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251223211937743693', '已完成', '2025-12-23 21:19:38', '2025-12-25 18:23:53');
INSERT INTO `tickets` VALUES (66, 'ORD20251223224630214', 3, '张建', '110101196505128976', '15973773378', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-23 22:46:31', '2025-12-09 12:00:00', '16D', '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251223224630740929', '待审核', '2025-12-23 22:46:31', '2025-12-23 22:46:31');
INSERT INTO `tickets` VALUES (67, 'ORD20251224102409234', 3, '李好呀呀呀', '123456789987654344', '15973773388', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-24 10:24:10', '2025-12-09 12:00:00', '16F', '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251224102409002753', '已完成', '2025-12-24 10:24:10', '2026-01-04 17:12:43');
INSERT INTO `tickets` VALUES (68, '2025122422001426530508089548', 3, '周瑾', '123456789987654378', '15973773390', 14, 'HU7615', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', '2025-12-09 18:00:00', '13E', '经济舱', 950.00, 215.00, 80.75, 1084.25, 'TKT20251224103820191450', '待审核', '2025-12-24 10:38:21', '2025-12-24 12:35:11');
INSERT INTO `tickets` VALUES (70, '2025122422001426530508101430', 3, '周瑾', '123456789987654378', '15973773390', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '17B', '经济舱', 1.00, 200.00, 55.00, 146.00, 'TKT20251224130157221442', '待审核', '2025-12-24 13:01:58', '2025-12-28 21:13:53');
INSERT INTO `tickets` VALUES (71, '2025122422001426530508093274', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '17C', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251224131203059254', '已出票', '2025-12-24 13:12:03', '2025-12-24 13:12:03');
INSERT INTO `tickets` VALUES (72, '2025122422001426530508098298', 3, '周瑾', '123456789987654378', '15973773390', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '17E', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251224131611649060', '已完成', '2025-12-24 13:16:11', '2025-12-24 23:34:18');
INSERT INTO `tickets` VALUES (73, '2025122422001426530508099765', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '17F', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251224132502411690', '待出行', '2025-12-24 13:25:02', '2025-12-24 23:42:19');
INSERT INTO `tickets` VALUES (74, '2025122422001426530508101431', 3, '张建', '110101196505128976', '13600136000', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '18A', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251224132641488264', '待处理', '2025-12-24 13:26:42', '2026-01-04 17:41:27');
INSERT INTO `tickets` VALUES (75, 'ORD20251224134647972', 3, '张建', '110101196505128976', '15973773356', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-24 13:46:47', '2025-12-09 12:00:00', '18E', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251224134647236804', 'deleted', '2025-12-24 13:46:47', '2025-12-25 18:23:13');
INSERT INTO `tickets` VALUES (76, 'ORD20251224135438198', 3, '张建', '110101196505128976', '15973773350', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '18F', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251224135438843551', '驳回', '2025-12-24 13:54:39', '2025-12-24 23:42:19');
INSERT INTO `tickets` VALUES (78, '2025122522001426530508111952', 3, '李好呀3', '123456789987654376', '15973773359', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '19B', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251225175302094541', '已完成', '2025-12-25 17:53:02', '2025-12-25 19:24:27');
INSERT INTO `tickets` VALUES (79, '2025122522001426530508113155', 3, '李好呀3', '123456789987654376', '15973773359', 15, 'HO1234', '上海虹桥国际机场', '成都天府国际机场', '上海虹桥国际机场 → 成都天府国际机场', '2025-12-22 16:00:00', '2025-12-22 20:00:00', '10A', '经济舱', 1000.00, 220.00, 0.00, 1220.00, 'TKT20251225180233802651', '已完成', '2025-12-25 18:02:34', '2025-12-25 18:41:02');
INSERT INTO `tickets` VALUES (80, 'ORD20251225183950527', 3, '李好呀3', '123456789987654376', '15973773359', 15, 'HO1234', '上海虹桥国际机场', '成都天府国际机场', '上海虹桥国际机场 → 成都天府国际机场', '2025-12-22 16:00:00', '2025-12-22 20:00:00', '10D', '经济舱', 1000.00, 220.00, 0.00, 1220.00, 'TKT20251225183950841370', '待审核', '2025-12-25 18:39:51', '2025-12-25 18:43:53');
INSERT INTO `tickets` VALUES (81, 'ORD20251225192259863', 3, '李好呀3', '123456789987654376', '15973773398', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '19C', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251225192259099157', '待处理', '2025-12-25 19:22:59', '2026-01-04 17:38:09');
INSERT INTO `tickets` VALUES (82, 'ORD20251226001807354', 3, '李好呀3', '123456789987654376', '', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '19D', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251226001807084275', '驳回', '2025-12-26 00:18:08', '2026-01-04 17:12:58');
INSERT INTO `tickets` VALUES (83, 'ORD_INTL_20251226_1', 20, 'Intl Passenger 1', 'IDINTL00000000001', '13900000001', 1131, 'INTL1001', 'LHR', 'PEK', 'LHR → PEK', '2025-12-30 09:00:00', '2025-12-30 23:30:00', '10A', 'economy', 420.00, 0.00, 0.00, 420.00, 'TKT1766758330', 'issued', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `tickets` VALUES (84, 'ORD_INTL_20251226_2', 21, 'Intl Passenger 2', 'IDINTL00000000002', '13900000002', 1132, 'INTL2002', 'JFK', 'PVG', 'JFK → PVG', '2025-12-31 10:00:00', '2026-01-01 02:30:00', '12A', 'economy', 480.00, 0.00, 0.00, 480.00, 'TKT1766758331', 'issued', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `tickets` VALUES (85, 'ORD_INTL_20251226_3', 22, 'Intl Passenger 3', 'IDINTL00000000003', '13900000003', 1133, 'INTL3003', 'NRT', 'CAN', 'NRT → CAN', '2025-12-29 13:00:00', '2025-12-29 18:30:00', '15A', 'economy', 320.00, 0.00, 0.00, 320.00, 'TKT1766758332', 'issued', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `tickets` VALUES (86, 'ORD_INTL_20251226_4', 23, 'Intl Passenger 4', 'IDINTL00000000004', '13900000004', 1134, 'INTL4004', 'SYD', 'SHA', 'SYD → SHA', '2025-12-28 08:00:00', '2025-12-28 20:30:00', '20A', 'economy', 550.00, 0.00, 0.00, 550.00, 'TKT1766758333', 'issued', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `tickets` VALUES (87, 'ORD_INTL_20251226_5', 24, 'Intl Passenger 5', 'IDINTL00000000005', '13900000005', 1135, 'INTL5005', 'DXB', 'SZX', 'DXB → SZX', '2025-12-27 02:00:00', '2025-12-27 14:30:00', '18A', 'economy', 390.00, 0.00, 0.00, 390.00, 'TKT1766758334', 'issued', '2025-12-26 22:12:10', '2025-12-26 22:12:10');
INSERT INTO `tickets` VALUES (88, '2025122722001426530508142492', 3, '李好呀3', '123456789987654376', '15973773359', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '19E', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251227223511070313', '已完成', '2025-12-27 22:35:12', '2025-12-27 22:45:03');
INSERT INTO `tickets` VALUES (89, 'ORD20251227224406449', 3, '李好呀3', '123456789987654376', '15973773396', 11, 'CA1201', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '20A', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251227224406865767', '已完成', '2025-12-27 22:44:06', '2025-12-28 21:17:31');
INSERT INTO `tickets` VALUES (90, '2025122822001426530508146267', 3, '你好呀', '123456789987654843', '15973773359', 11, 'CA1201', 'PKX', 'PVG', 'PKX → PVG', '2025-12-09 08:00:00', '2025-12-09 12:00:00', '20B', '经济舱', 1.00, 200.00, 50.00, 151.00, 'TKT20251228202148572160', 'deleted', '2025-12-28 20:21:48', '2025-12-28 20:24:58');
INSERT INTO `tickets` VALUES (91, 'ORD20251228211536486', 3, '李好呀3', '123456789987654376', '15973773352', 14, 'HU7615', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', '2025-12-09 12:00:00', '13F', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251228211536945213', '已完成', '2025-12-28 21:15:37', '2026-01-04 15:08:35');
INSERT INTO `tickets` VALUES (92, 'ORD20251229171752243', 3, '李好呀呀呀', '123456789987654344', '', 14, 'HU7615', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', '2025-12-09 12:00:00', '14A', '经济舱', 1.00, 0.00, 0.00, 1.00, 'TKT20251229171752932962', '已出票', '2025-12-29 17:17:53', '2026-01-04 17:12:43');
INSERT INTO `tickets` VALUES (93, 'ORD20251230100621191', 3, '李好呀3', '123456789987654376', '15973773399', 14, 'HU7615', '北京首都国际机场', '上海虹桥国际机场', '北京首都国际机场 → 上海虹桥国际机场', '2025-12-09 14:00:00', '2025-12-09 12:00:00', '14B', '经济舱', 1.00, 200.00, 0.00, 201.00, 'TKT20251230100621834774', '已出票', '2025-12-30 10:06:22', '2026-01-04 15:08:35');
INSERT INTO `tickets` VALUES (94, '2026010322001426530508203237', 3, '张浩然', '110101201512036215', '15973773354', 14, 'CA1501', 'PEK', 'SHA', 'PEK → SHA', '2026-01-07 08:30:00', '2026-01-07 10:55:00', '14D', '经济舱', 2150.00, 215.00, 0.00, 2365.00, 'TKT20260103113704209041', '待退款', '2026-01-03 11:37:05', '2026-01-03 13:42:05');
INSERT INTO `tickets` VALUES (95, '2026010322001426530508203238', 3, '张建', '110101196505128976', '13600136000', 14, 'CA1501', 'PEK', 'SHA', 'PEK → SHA', '2026-01-07 08:30:00', '2026-01-07 10:55:00', '14E', '经济舱', 2150.00, 215.00, 0.00, 2365.00, 'TKT20260103113815051136', '已完成', '2026-01-03 11:38:16', '2026-01-03 13:40:59');
INSERT INTO `tickets` VALUES (96, 'ORD20260103134003624', 3, '张建', '110101196505128976', '15973773356', 11, 'CA1519', 'PEK', 'SHA', 'PEK → SHA', '2026-01-07 09:30:00', '2026-01-07 10:55:00', '20E', '经济舱', 2150.00, 215.00, 0.00, 2150.00, 'TKT20260103134003851217', '已出票', '2026-01-03 13:40:04', '2026-01-03 13:40:59');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'passenger',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active',
  `registration_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `id_card`(`id_card` ASC) USING BTREE,
  UNIQUE INDEX `idx_users_email_unique`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'demo', '12345678', '511025000000000000', '15828824106', '系统管理员', 'admin', 'active', '2025-12-03 17:29:59', '2025-12-28 14:26:25', NULL);
INSERT INTO `users` VALUES (2, 'occ_admin', 'occ123', '411025000000000000', '13767289298', '运营管理员', 'operator', 'active', '2025-12-03 17:29:59', '2025-12-03 17:31:33', NULL);
INSERT INTO `users` VALUES (3, 'vip_pax', 'pax123', '311025000000000000', '12475928103', '普通乘客', 'passenger', 'active', '2025-12-03 17:29:59', '2025-12-03 18:09:29', NULL);
INSERT INTO `users` VALUES (8, 'user1', 'password123', '511025000000000001', '15828824111', '老吴', 'passenger', 'active', '2025-12-03 18:47:02', '2025-12-03 18:47:02', NULL);
INSERT INTO `users` VALUES (9, 'user2', '123456', '511055000000000000', '19234567802', '老六', 'passenger', 'active', '2025-12-03 19:37:30', '2025-12-03 19:37:30', NULL);
INSERT INTO `users` VALUES (11, 'toonuy6493', 'Password123', NULL, '15237531094', '陈磊', 'passenger', 'active', '2025-12-21 19:57:27', '2025-12-21 19:57:27', NULL);
INSERT INTO `users` VALUES (12, 'tddptf4068', 'Password123', NULL, '15959159433', '钱芳', 'passenger', 'active', '2025-12-21 19:57:27', '2025-12-21 19:57:27', NULL);
INSERT INTO `users` VALUES (13, 'tddhsv2629', 'Password123', NULL, '18461177793', '钱娟', 'passenger', 'active', '2025-12-21 19:57:27', '2025-12-21 19:57:27', NULL);
INSERT INTO `users` VALUES (14, 'isglm1k09634', 'Password123', NULL, '18470430177', '张浩敏', 'passenger', 'active', '2025-12-21 19:57:27', '2025-12-21 19:57:27', NULL);
INSERT INTO `users` VALUES (15, 'ggehsvtw5173', 'Password123', NULL, '18281468557', '华娟静', 'passenger', 'active', '2025-12-21 19:57:27', '2025-12-21 19:57:27', NULL);
INSERT INTO `users` VALUES (16, 'lfsfgdry3898', 'Password123', NULL, '15717793547', '沈丽超', 'passenger', 'active', '2025-12-21 19:57:27', '2025-12-21 19:57:27', NULL);
INSERT INTO `users` VALUES (17, 'mtntum6983', 'Password123', NULL, '18447147554', '王霞', 'passenger', 'active', '2025-12-21 19:59:24', '2025-12-21 19:59:24', NULL);
INSERT INTO `users` VALUES (18, 'gh7g7u9471', 'Password123', NULL, '15812139514', '卫刚', 'passenger', 'active', '2025-12-21 20:04:51', '2025-12-21 20:04:51', NULL);
INSERT INTO `users` VALUES (19, 'r2ymuqtu6100', 'Password123', NULL, '15135107607', '褚玲霞', 'passenger', 'active', '2025-12-21 20:07:47', '2025-12-21 20:07:47', NULL);
INSERT INTO `users` VALUES (20, 'intl_user_1', 'pass', 'IDINTL00000000001', '13900000001', 'Intl Passenger 1', 'passenger', 'active', '2025-12-26 22:12:10', '2025-12-26 22:12:10', NULL);
INSERT INTO `users` VALUES (21, 'intl_user_2', 'pass', 'IDINTL00000000002', '13900000002', 'Intl Passenger 2', 'passenger', 'active', '2025-12-26 22:12:10', '2025-12-26 22:12:10', NULL);
INSERT INTO `users` VALUES (22, 'intl_user_3', 'pass', 'IDINTL00000000003', '13900000003', 'Intl Passenger 3', 'passenger', 'active', '2025-12-26 22:12:10', '2025-12-26 22:12:10', NULL);
INSERT INTO `users` VALUES (23, 'intl_user_4', 'pass', 'IDINTL00000000004', '13900000004', 'Intl Passenger 4', 'passenger', 'active', '2025-12-26 22:12:10', '2025-12-26 22:12:10', NULL);
INSERT INTO `users` VALUES (24, 'intl_user_5', 'pass', 'IDINTL00000000005', '13900000005', 'Intl Passenger 5', 'passenger', 'active', '2025-12-26 22:12:10', '2025-12-26 22:12:10', NULL);

SET FOREIGN_KEY_CHECKS = 1;
