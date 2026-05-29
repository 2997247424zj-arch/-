/*
 Navicat Premium Data Transfer

 Source Server         : 8888
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : b

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 17/06/2025 15:08:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart_item
-- ----------------------------
DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item`  (
  `id` bigint NOT NULL COMMENT '商品ID(关联product表)',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述',
  `discount` decimal(3, 1) NULL DEFAULT 0.0 COMMENT '折扣(0-无折扣)',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名(关联sys_user表)',
  PRIMARY KEY (`id`, `username`) USING BTREE,
  INDEX `username`(`username` ASC) USING BTREE,
  CONSTRAINT `cart_item_ibfk_1` FOREIGN KEY (`id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `cart_item_ibfk_2` FOREIGN KEY (`username`) REFERENCES `sys_user` (`username`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart_item
-- ----------------------------
INSERT INTO `cart_item` VALUES (4, '农夫山泉550ml', 2.00, 2.50, '天然饮用水，源自优质水源', 8.0, 2, 'lihaoya');
INSERT INTO `cart_item` VALUES (5, '统一冰红茶500ml', 3.50, 4.00, '柠檬味茶饮料，冰爽口感', 8.8, 2, 'lihaoya');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  INDEX `order_item_ibfk_1`(`order_id` ASC) USING BTREE,
  CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 119 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 182, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (2, 182, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (3, 183, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (4, 183, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (5, 184, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (6, 184, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (7, 185, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (8, 185, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (9, 186, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (10, 186, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (11, 187, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (12, 187, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (13, 188, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (14, 188, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (15, 189, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (16, 189, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (17, 190, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (18, 190, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (19, 191, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (20, 191, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (21, 192, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (22, 192, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (23, 193, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (24, 193, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (25, 194, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (26, 194, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (27, 195, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (28, 195, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (29, 196, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (30, 196, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (31, 197, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (32, 197, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (33, 198, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (34, 198, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (35, 199, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (36, 199, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (37, 200, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (38, 200, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (39, 201, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (40, 201, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (41, 202, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (42, 202, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (43, 203, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (44, 203, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (45, 204, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (46, 204, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (47, 205, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (48, 205, 3, 1, 3.00);
INSERT INTO `order_item` VALUES (49, 206, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (50, 206, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (51, 207, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (52, 207, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (53, 208, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (54, 208, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (55, 209, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (56, 209, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (57, 210, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (58, 210, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (59, 211, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (60, 211, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (61, 212, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (62, 212, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (63, 213, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (64, 214, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (65, 214, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (66, 215, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (67, 215, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (68, 216, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (69, 216, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (70, 217, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (71, 217, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (72, 218, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (73, 218, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (74, 219, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (75, 219, 3, 1, 3.00);
INSERT INTO `order_item` VALUES (76, 220, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (77, 220, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (78, 221, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (79, 221, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (80, 222, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (81, 222, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (82, 223, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (83, 223, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (84, 224, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (85, 224, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (86, 225, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (87, 225, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (88, 226, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (89, 226, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (90, 227, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (91, 228, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (92, 228, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (93, 229, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (94, 229, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (95, 230, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (96, 230, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (97, 231, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (98, 232, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (99, 232, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (100, 233, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (101, 233, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (102, 234, 7, 1, 29.90);
INSERT INTO `order_item` VALUES (103, 235, 7, 1, 29.90);
INSERT INTO `order_item` VALUES (104, 235, 4, 1, 2.00);
INSERT INTO `order_item` VALUES (105, 236, 7, 2, 29.90);
INSERT INTO `order_item` VALUES (106, 236, 5, 2, 3.50);
INSERT INTO `order_item` VALUES (107, 237, 7, 1, 29.90);
INSERT INTO `order_item` VALUES (108, 237, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (109, 238, 7, 1, 29.90);
INSERT INTO `order_item` VALUES (110, 238, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (111, 239, 7, 1, 29.90);
INSERT INTO `order_item` VALUES (112, 239, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (113, 240, 7, 2, 29.90);
INSERT INTO `order_item` VALUES (114, 240, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (115, 241, 7, 1, 29.90);
INSERT INTO `order_item` VALUES (116, 241, 5, 1, 3.50);
INSERT INTO `order_item` VALUES (117, 242, 7, 1, 29.90);
INSERT INTO `order_item` VALUES (118, 242, 5, 1, 3.50);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `cashier_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收银员用户名',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '商品总额',
  `discount` decimal(10, 2) NOT NULL COMMENT '优惠金额',
  `total` decimal(10, 2) NOT NULL COMMENT '应付金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付方式',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `fk_cashier_name`(`cashier_name` ASC) USING BTREE,
  CONSTRAINT `fk_cashier_name` FOREIGN KEY (`cashier_name`) REFERENCES `sys_user` (`username`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 243 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD20240601001', 'zhangsan', 10.50, 0.50, 10.00, '现金支付', '2025-05-25 10:24:15');
INSERT INTO `orders` VALUES (2, 'ORD20240601002', 'zhangsan', 6.00, 0.00, 6.00, '刷卡支付', '2025-05-25 10:24:15');
INSERT INTO `orders` VALUES (3, 'ORD20240601003', 'lisi', 9.50, 1.00, 8.50, '手机支付', '2025-05-25 10:24:15');
INSERT INTO `orders` VALUES (130, 'ORD1748159361105', 'admin', 5.50, 0.00, 5.50, '手机支付', '2025-05-25 15:49:21');
INSERT INTO `orders` VALUES (131, 'ORD1748159368507', 'admin', 8.50, 0.00, 8.50, '刷卡支付', '2025-05-25 15:49:28');
INSERT INTO `orders` VALUES (132, 'ORD1748159373636', 'admin', 18.00, 0.00, 18.00, '现金支付', '2025-05-25 15:49:33');
INSERT INTO `orders` VALUES (133, 'ORD1748160205492', 'admin', 8.50, 0.00, 8.50, '手机支付', '2025-05-25 16:03:25');
INSERT INTO `orders` VALUES (134, 'ORD1748178498532', 'admin', 3.00, 0.00, 3.00, '手机支付', '2025-05-25 21:08:18');
INSERT INTO `orders` VALUES (135, 'ORD1748179652030', 'admin', 2.00, 0.00, 2.00, '手机支付', '2025-05-25 21:27:32');
INSERT INTO `orders` VALUES (136, 'ORD1748179768097', 'admin', 3.50, 1.00, 2.50, '现金支付', '2025-05-25 21:29:28');
INSERT INTO `orders` VALUES (137, 'ORD1748179932342', 'admin', 2.00, 0.00, 2.00, '刷卡支付', '2025-05-25 21:32:12');
INSERT INTO `orders` VALUES (138, 'ORD1748230180970', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-26 11:29:40');
INSERT INTO `orders` VALUES (139, 'ORD1748230824583', 'lisi', 7.50, 0.00, 7.50, '现金支付', '2025-05-26 11:40:24');
INSERT INTO `orders` VALUES (140, 'ORD1748231389962', 'lisi', 3.50, 0.00, 3.50, '手机支付', '2025-05-26 11:49:49');
INSERT INTO `orders` VALUES (141, 'ORD1748258037572', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-26 19:13:57');
INSERT INTO `orders` VALUES (142, 'ORD1748259944014', 'lisi', 10.00, 0.00, 10.00, '手机支付', '2025-05-26 19:45:44');
INSERT INTO `orders` VALUES (143, 'ORD1748260576900', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-26 19:56:16');
INSERT INTO `orders` VALUES (144, 'ORD1748260658903', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-26 19:57:38');
INSERT INTO `orders` VALUES (145, 'ORD1748261568334', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-26 20:12:48');
INSERT INTO `orders` VALUES (146, 'ORD1748262050577', 'lisi', 12.00, 0.00, 12.00, '手机支付', '2025-05-26 20:20:50');
INSERT INTO `orders` VALUES (147, 'ORD1748262382026', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-26 20:26:22');
INSERT INTO `orders` VALUES (148, 'ORD1748266288196', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-26 21:31:28');
INSERT INTO `orders` VALUES (149, 'ORD1748266719452', 'lisi', 11.50, 0.00, 11.50, '手机支付', '2025-05-26 21:38:39');
INSERT INTO `orders` VALUES (150, 'ORD1748267443255', 'lisi', 5.00, 0.00, 5.00, '现金支付', '2025-05-26 21:50:43');
INSERT INTO `orders` VALUES (151, 'ORD1748272470239', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-26 23:14:30');
INSERT INTO `orders` VALUES (152, 'ORD1748302775170', 'lisi', 11.00, 0.00, 11.00, '手机支付', '2025-05-27 07:39:35');
INSERT INTO `orders` VALUES (153, 'ORD1748303360277', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-27 07:49:20');
INSERT INTO `orders` VALUES (154, 'ORD1748310189007', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-27 09:43:09');
INSERT INTO `orders` VALUES (155, 'ORD1748331315250', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-27 15:35:15');
INSERT INTO `orders` VALUES (156, 'ORD1748342004654', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-27 18:33:24');
INSERT INTO `orders` VALUES (157, 'ORD1748349339085', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-27 20:35:39');
INSERT INTO `orders` VALUES (158, 'ORD1748350366221', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-27 20:52:46');
INSERT INTO `orders` VALUES (159, 'ORD1748350556891', 'lisi', 7.00, 0.00, 7.00, '手机支付', '2025-05-27 20:55:56');
INSERT INTO `orders` VALUES (160, 'ORD1748353651906', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-27 21:47:31');
INSERT INTO `orders` VALUES (161, 'ORD1748356192781', 'lisi', 6.00, 0.00, 6.00, '手机支付', '2025-05-27 22:29:52');
INSERT INTO `orders` VALUES (162, 'ORD1748360556657', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-27 23:42:36');
INSERT INTO `orders` VALUES (163, 'ORD1748397489321', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-28 09:58:09');
INSERT INTO `orders` VALUES (164, 'ORD1748397789950', 'lisi', 2.00, 0.00, 2.00, '手机支付', '2025-05-28 10:03:09');
INSERT INTO `orders` VALUES (165, 'ORD1748403584051', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-28 11:39:44');
INSERT INTO `orders` VALUES (166, 'ORD1748404575124', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-28 11:56:15');
INSERT INTO `orders` VALUES (167, 'ORD1748407766444', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-28 12:49:26');
INSERT INTO `orders` VALUES (168, 'ORD1748409087644', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-28 13:11:27');
INSERT INTO `orders` VALUES (169, 'ORD1748410026476', 'lisi', 3.50, 0.00, 3.50, '手机支付', '2025-05-28 13:27:06');
INSERT INTO `orders` VALUES (170, 'ORD1748416168080', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-28 15:09:28');
INSERT INTO `orders` VALUES (171, 'ORD1748436650736', 'lisi', 7.50, 0.00, 7.50, '手机支付', '2025-05-28 20:50:50');
INSERT INTO `orders` VALUES (172, 'ORD1748493058427', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-29 12:30:58');
INSERT INTO `orders` VALUES (173, 'ORD1748495441046', 'lisi', 3.50, 0.00, 3.50, '手机支付', '2025-05-29 13:10:41');
INSERT INTO `orders` VALUES (174, 'ORD1748495823233', 'lisi', 10.50, 0.00, 10.50, '手机支付', '2025-05-29 13:17:03');
INSERT INTO `orders` VALUES (175, 'ORD1748495878679', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-29 13:17:58');
INSERT INTO `orders` VALUES (176, 'ORD1748502632156', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-29 15:10:32');
INSERT INTO `orders` VALUES (177, 'ORD1748503445641', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-29 15:24:05');
INSERT INTO `orders` VALUES (178, 'ORD1748505069109', 'lisi', 9.00, 0.00, 9.00, '手机支付', '2025-05-29 15:51:09');
INSERT INTO `orders` VALUES (179, 'ORD1748515815107', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-29 18:50:15');
INSERT INTO `orders` VALUES (180, 'ORD1748652618145', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 08:50:18');
INSERT INTO `orders` VALUES (182, 'ORD1748653737387', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 09:08:57');
INSERT INTO `orders` VALUES (183, 'ORD1748653876197', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 09:11:16');
INSERT INTO `orders` VALUES (184, 'ORD1748654273701', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 09:17:53');
INSERT INTO `orders` VALUES (185, 'ORD1748655065410', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 09:31:05');
INSERT INTO `orders` VALUES (186, 'ORD1748656604353', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 09:56:44');
INSERT INTO `orders` VALUES (187, 'ORD1748656942134', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 10:02:22');
INSERT INTO `orders` VALUES (188, 'ORD1748658408180', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 10:26:48');
INSERT INTO `orders` VALUES (189, 'ORD1748659977541', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 10:52:57');
INSERT INTO `orders` VALUES (190, 'ORD1748663434095', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 11:50:34');
INSERT INTO `orders` VALUES (191, 'ORD1748665016819', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 12:16:56');
INSERT INTO `orders` VALUES (192, 'ORD1748673855193', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 14:44:15');
INSERT INTO `orders` VALUES (193, 'ORD1748678702075', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 16:05:02');
INSERT INTO `orders` VALUES (194, 'ORD1748678878869', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 16:07:58');
INSERT INTO `orders` VALUES (195, 'ORD1748678927051', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 16:08:47');
INSERT INTO `orders` VALUES (196, 'ORD1748679034696', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 16:10:34');
INSERT INTO `orders` VALUES (197, 'ORD1748679125993', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 16:12:05');
INSERT INTO `orders` VALUES (198, 'ORD1748679297901', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 16:14:57');
INSERT INTO `orders` VALUES (199, 'ORD1748679412696', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 16:16:52');
INSERT INTO `orders` VALUES (200, 'ORD1748688760252', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 18:52:40');
INSERT INTO `orders` VALUES (201, 'ORD1748689217650', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 19:00:17');
INSERT INTO `orders` VALUES (202, 'ORD1748693452352', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 20:10:52');
INSERT INTO `orders` VALUES (203, 'ORD1748696869439', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 21:07:49');
INSERT INTO `orders` VALUES (204, 'ORD1748697520450', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-05-31 21:18:40');
INSERT INTO `orders` VALUES (205, 'ORD1748701955177', 'lisi', 6.50, 0.00, 6.50, '手机支付', '2025-05-31 22:32:35');
INSERT INTO `orders` VALUES (206, 'ORD1748741354523', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 09:29:14');
INSERT INTO `orders` VALUES (207, 'ORD1748741753812', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 09:35:53');
INSERT INTO `orders` VALUES (208, 'ORD1748744451933', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 10:20:51');
INSERT INTO `orders` VALUES (209, 'ORD1748746782447', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 10:59:42');
INSERT INTO `orders` VALUES (210, 'ORD1748747774466', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 11:16:14');
INSERT INTO `orders` VALUES (211, 'ORD1748756919940', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 13:48:39');
INSERT INTO `orders` VALUES (212, 'ORD1748771119428', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 17:45:19');
INSERT INTO `orders` VALUES (213, 'ORD1748771749061', 'lisi', 3.50, 0.00, 3.50, '手机支付', '2025-06-01 17:55:49');
INSERT INTO `orders` VALUES (214, 'ORD1748772358050', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 18:05:58');
INSERT INTO `orders` VALUES (215, 'ORD1748776188445', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 19:09:48');
INSERT INTO `orders` VALUES (216, 'ORD1748780117507', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 20:15:17');
INSERT INTO `orders` VALUES (217, 'ORD1748782989152', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-01 21:03:09');
INSERT INTO `orders` VALUES (218, 'ORD1748825872807', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-02 08:57:52');
INSERT INTO `orders` VALUES (219, 'ORD1748832335945', 'lisi', 6.50, 0.00, 6.50, '手机支付', '2025-06-02 10:45:35');
INSERT INTO `orders` VALUES (220, 'ORD1748846286861', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-02 14:38:06');
INSERT INTO `orders` VALUES (221, 'ORD1748854296144', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-02 16:51:36');
INSERT INTO `orders` VALUES (222, 'ORD1748868171094', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-02 20:42:51');
INSERT INTO `orders` VALUES (223, 'ORD1748911049981', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-03 08:37:29');
INSERT INTO `orders` VALUES (224, 'ORD1748915695511', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-03 09:54:55');
INSERT INTO `orders` VALUES (225, 'ORD1748935796726', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-03 15:29:56');
INSERT INTO `orders` VALUES (226, 'ORD1748937126732', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-03 15:52:06');
INSERT INTO `orders` VALUES (227, 'ORD1748948659238', 'lisi', 3.50, 0.00, 3.50, '手机支付', '2025-06-03 19:04:19');
INSERT INTO `orders` VALUES (228, 'ORD1749104953957', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-05 14:29:13');
INSERT INTO `orders` VALUES (229, 'ORD1749532713699', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-10 13:18:33');
INSERT INTO `orders` VALUES (230, 'ORD1749542757961', 'lisi', 5.50, 0.00, 5.50, '现金支付', '2025-06-10 16:05:57');
INSERT INTO `orders` VALUES (231, 'ORD1749544596459', 'lisi', 3.50, 0.00, 3.50, '刷卡支付', '2025-06-10 16:36:36');
INSERT INTO `orders` VALUES (232, 'ORD1749548079881', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-10 17:34:39');
INSERT INTO `orders` VALUES (233, 'ORD1749553654863', 'lisi', 5.50, 0.00, 5.50, '手机支付', '2025-06-10 19:07:34');
INSERT INTO `orders` VALUES (234, 'ORD1749564655969', 'lisi', 29.90, 0.00, 29.90, '手机支付', '2025-06-10 22:10:55');
INSERT INTO `orders` VALUES (235, 'ORD1749945993237', 'lisi', 31.90, 0.00, 31.90, '手机支付', '2025-06-15 08:06:33');
INSERT INTO `orders` VALUES (236, 'ORD1750034622156', 'lisi', 66.80, 0.00, 66.80, '现金支付', '2025-06-16 08:43:42');
INSERT INTO `orders` VALUES (237, 'ORD1750036274137', 'uuuu', 33.40, 0.00, 33.40, '手机支付', '2025-06-16 09:11:14');
INSERT INTO `orders` VALUES (238, 'ORD1750037701930', 'uuuu', 33.40, 0.00, 33.40, '手机支付', '2025-06-16 09:35:01');
INSERT INTO `orders` VALUES (239, 'ORD1750053006643', 'lisi', 33.40, 0.00, 33.40, '手机支付', '2025-06-16 13:50:06');
INSERT INTO `orders` VALUES (240, 'ORD1750054437530', 'lisi', 63.30, 0.00, 63.30, '现金支付', '2025-06-16 14:13:57');
INSERT INTO `orders` VALUES (241, 'ORD1750071617007', 'lisi', 33.40, 0.00, 33.40, '手机支付', '2025-06-16 19:00:17');
INSERT INTO `orders` VALUES (242, 'ORD1750143881235', 'lisi', 33.40, 0.00, 33.40, '手机支付', '2025-06-17 15:04:41');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `barcode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条码',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片路径',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `is_new` tinyint(1) NULL DEFAULT 0 COMMENT '是否新品(0-否 1-是)',
  `discount` decimal(3, 1) NULL DEFAULT 0.0 COMMENT '折扣(0-无折扣)',
  `tags` json NULL COMMENT '商品标签',
  `specs` json NULL COMMENT '规格参数',
  `stock` int NULL DEFAULT 0 COMMENT '当前库存数量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `barcode`(`barcode` ASC) USING BTREE,
  INDEX `fk_product_category`(`category_id` ASC) USING BTREE,
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, '可口可乐330ml', '6901234567891', 3.00, 'src/images/coke.jpg', '2025-05-25 10:24:02', '2025-06-10 15:32:49', '经典碳酸饮料，畅爽口感', 3.50, 1, 1, 8.6, '[\"饮料\", \"碳酸\", \"热门\"]', '{\"包装\": \"罐装\", \"容量\": \"330ml\", \"保质期\": \"12个月\"}', 20);
INSERT INTO `product` VALUES (2, '康师傅红烧牛肉面', '6901234567892', 4.50, 'src/images/noodles.jpg', '2025-05-25 10:24:02', '2025-06-10 15:32:49', '经典红烧牛肉味方便面，浓郁汤底', 5.00, 2, 0, 9.0, '[\"方便面\", \"速食\", \"牛肉味\"]', '{\"包装\": \"袋装\", \"保质期\": \"6个月\", \"净含量\": \"103g\"}', 20);
INSERT INTO `product` VALUES (3, '百事可乐330ml', '6901234567893', 3.00, 'src/images/pepsi.jpg', '2025-05-25 10:24:02', '2025-06-10 15:32:49', '经典美式碳酸饮料，清爽解渴', 3.50, 1, 1, 8.6, '[\"饮料\", \"碳酸\", \"国际品牌\"]', '{\"包装\": \"罐装\", \"容量\": \"330ml\", \"保质期\": \"12个月\"}', 30);
INSERT INTO `product` VALUES (4, '农夫山泉550ml', '6901234567894', 2.00, 'src/images/water.jpg', '2025-05-25 10:24:02', '2025-06-10 15:32:49', '天然饮用水，源自优质水源', 2.50, 3, 0, 8.0, '[\"饮用水\", \"天然\", \"健康\"]', '{\"容量\": \"550ml\", \"保质期\": \"24个月\", \"水源地\": \"千岛湖\"}', 40);
INSERT INTO `product` VALUES (5, '统一冰红茶500ml', '6901234567895', 3.50, 'src/images/ice_red_tea.jpg', '2025-05-25 10:24:02', '2025-06-10 15:32:49', '柠檬味茶饮料，冰爽口感', 4.00, 4, 0, 8.8, '[\"茶饮料\", \"柠檬味\", \"解渴\"]', '{\"容量\": \"500ml\", \"糖分\": \"低糖\", \"保质期\": \"12个月\"}', 50);
INSERT INTO `product` VALUES (7, '清风原木纯品抽纸', '6941234567890', 29.90, 'src/images/QingFeng.jpg', '2025-06-10 19:49:11', '2025-06-10 19:50:56', '清风原木纯品3层120抽面巾纸，天然木浆制造，柔软亲肤', 32.90, 6, 0, 9.1, '[\"生活用品\", \"纸巾\", \"促销\"]', '{\"材质\": \"原生木浆\", \"规格\": \"3层120抽×24包\", \"香味\": \"无香\", \"适用场景\": \"家用\"}', 25);

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `sort` int NULL DEFAULT 0 COMMENT '排序权重',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态(0-禁用 1-启用)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_category
-- ----------------------------
INSERT INTO `product_category` VALUES (1, '饮料', 1, 1);
INSERT INTO `product_category` VALUES (2, '方便面', 2, 1);
INSERT INTO `product_category` VALUES (3, '饮用水', 3, 1);
INSERT INTO `product_category` VALUES (4, '茶饮料', 4, 1);
INSERT INTO `product_category` VALUES (5, '零食', 5, 1);
INSERT INTO `product_category` VALUES (6, '日用品', 6, 1);
INSERT INTO `product_category` VALUES (7, '生鲜', 7, 1);
INSERT INTO `product_category` VALUES (8, '家居', 8, 1);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（明文存储，测试用）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户角色（admin/cashier/user/inventory）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (28, 'zhangsan', '123456', 'cashier', '2025-05-25 10:23:48', '2025-05-25 10:23:48');
INSERT INTO `sys_user` VALUES (29, 'lisi', '123456', 'cashier', '2025-05-25 10:23:48', '2025-05-25 10:23:48');
INSERT INTO `sys_user` VALUES (30, 'admin', '123eeeee', 'admin', '2025-05-25 10:23:48', '2025-05-27 17:44:58');
INSERT INTO `sys_user` VALUES (32, 'lihaoya', '22222e', 'user', '2025-05-25 13:28:34', '2025-06-17 11:50:54');
INSERT INTO `sys_user` VALUES (33, 'admin88', '1234567', 'admin', '2025-05-25 15:24:26', '2025-05-25 15:24:26');
INSERT INTO `sys_user` VALUES (37, 'uuuuu', '123eeeee', 'cashier', '2025-06-10 15:21:13', '2025-06-10 15:21:13');
INSERT INTO `sys_user` VALUES (38, 'uuuu', '123eeeee', 'cashier', '2025-06-10 15:21:53', '2025-06-10 15:21:53');
INSERT INTO `sys_user` VALUES (39, 'yyyy', '123eeeee', 'cashier', '2025-06-10 17:34:00', '2025-06-10 19:18:55');
INSERT INTO `sys_user` VALUES (40, 'yyu', '123eeeee', 'admin', '2025-06-10 17:34:18', '2025-06-10 19:18:44');
INSERT INTO `sys_user` VALUES (41, '9897', '123eeeee', 'cashier', '2025-06-10 19:29:23', '2025-06-10 19:45:19');
INSERT INTO `sys_user` VALUES (42, 'adm778', '123eeeee', 'cashier', '2025-06-10 20:24:29', '2025-06-10 20:24:52');
INSERT INTO `sys_user` VALUES (43, 'lihaoyas', '11111e', 'user', '2025-06-16 15:15:02', '2025-06-17 11:51:24');
INSERT INTO `sys_user` VALUES (44, 'lihaoyas444', '88888u', 'cashier', '2025-06-16 18:59:37', '2025-06-16 18:59:37');

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联用户名（同时也是收货人姓名）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `detailed_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `fk_user_address_name`(`name` ASC) USING BTREE,
  CONSTRAINT `fk_user_address_name` FOREIGN KEY (`name`) REFERENCES `sys_user` (`username`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_address
-- ----------------------------
INSERT INTO `user_address` VALUES (24, 'lihaoyas', 'lihaoyas', '15973773352', '4444', '湖南省 / 张家界市 / 永定区', '4444', '9999999880');
INSERT INTO `user_address` VALUES (26, 'lihaoya4', 'lihaoya', '15973773352', '4444', '湖南省 / 张家界市 / 永定区', '4444', '9999999999998');
INSERT INTO `user_address` VALUES (38, '0000000', 'lihaoya', '15973773352', '湖南省', '益阳市', '沅江市', '9999999999');
INSERT INTO `user_address` VALUES (39, '9999', 'lihaoyas', '15973773352', '湖南省', '益阳市', '沅江市', '333333');

-- ----------------------------
-- Table structure for user_favorites
-- ----------------------------
DROP TABLE IF EXISTS `user_favorites`;
CREATE TABLE `user_favorites`  (
  `product_id` bigint NOT NULL COMMENT '商品ID（关联product.id）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（关联product.name）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品描述（关联product.description）',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格（关联product.price）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（关联sys_user.username）',
  PRIMARY KEY (`product_id`, `username`) USING BTREE,
  INDEX `fk_user_favorites_username`(`username` ASC) USING BTREE,
  CONSTRAINT `fk_user_favorites_username` FOREIGN KEY (`username`) REFERENCES `sys_user` (`username`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_favorites_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_favorites
-- ----------------------------
INSERT INTO `user_favorites` VALUES (4, '农夫山泉550ml', '天然饮用水，源自优质水源', 2.00, '2025-06-16 21:39:09', 'lihaoyas');
INSERT INTO `user_favorites` VALUES (5, '统一冰红茶500ml', '柠檬味茶饮料，冰爽口感', 3.50, '2025-06-16 21:37:31', 'lihaoyas');

-- ----------------------------
-- Table structure for user_order_items
-- ----------------------------
DROP TABLE IF EXISTS `user_order_items`;
CREATE TABLE `user_order_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID（主键，自增）',
  `order_id` bigint NOT NULL COMMENT '订单ID（关联user_orders表）',
  `product_id` bigint NOT NULL COMMENT '商品ID（关联product表）',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `user_order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `user_orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_order_items
-- ----------------------------
INSERT INTO `user_order_items` VALUES (1, 1, 4, '农夫山泉550ml', 2.00, 1, 2.00);
INSERT INTO `user_order_items` VALUES (2, 1, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (3, 2, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (4, 3, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (5, 4, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (6, 5, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (7, 6, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (8, 7, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (9, 8, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (10, 9, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (11, 10, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (12, 11, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (13, 12, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (14, 13, 4, '农夫山泉550ml', 2.00, 1, 2.00);
INSERT INTO `user_order_items` VALUES (15, 13, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (16, 14, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (17, 15, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (18, 16, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (19, 17, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (20, 18, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (21, 19, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (22, 20, 4, '农夫山泉550ml', 2.00, 2, 4.00);
INSERT INTO `user_order_items` VALUES (23, 21, 5, '统一冰红茶500ml', 3.50, 1, 3.50);
INSERT INTO `user_order_items` VALUES (24, 22, 5, '统一冰红茶500ml', 3.50, 2, 7.00);
INSERT INTO `user_order_items` VALUES (25, 23, 4, '农夫山泉550ml', 2.00, 4, 8.00);
INSERT INTO `user_order_items` VALUES (26, 24, 5, '统一冰红茶500ml', 3.50, 2, 7.00);
INSERT INTO `user_order_items` VALUES (27, 25, 5, '统一冰红茶500ml', 3.50, 2, 7.00);
INSERT INTO `user_order_items` VALUES (28, 26, 5, '统一冰红茶500ml', 3.50, 2, 7.00);
INSERT INTO `user_order_items` VALUES (29, 27, 5, '统一冰红茶500ml', 3.50, 2, 7.00);
INSERT INTO `user_order_items` VALUES (30, 28, 5, '统一冰红茶500ml', 3.50, 2, 7.00);
INSERT INTO `user_order_items` VALUES (31, 29, 4, '农夫山泉550ml', 2.00, 1, 2.00);
INSERT INTO `user_order_items` VALUES (32, 30, 7, '清风原木纯品抽纸', 29.90, 2, 59.80);
INSERT INTO `user_order_items` VALUES (33, 31, 7, '清风原木纯品抽纸', 29.90, 1, 29.90);
INSERT INTO `user_order_items` VALUES (34, 32, 7, '清风原木纯品抽纸', 29.90, 2, 59.80);
INSERT INTO `user_order_items` VALUES (35, 33, 7, '清风原木纯品抽纸', 29.90, 2, 59.80);
INSERT INTO `user_order_items` VALUES (36, 34, 7, '清风原木纯品抽纸', 29.90, 2, 59.80);
INSERT INTO `user_order_items` VALUES (37, 35, 7, '清风原木纯品抽纸', 29.90, 4, 119.60);
INSERT INTO `user_order_items` VALUES (38, 36, 5, '统一冰红茶500ml', 3.50, 2, 7.00);

-- ----------------------------
-- Table structure for user_orders
-- ----------------------------
DROP TABLE IF EXISTS `user_orders`;
CREATE TABLE `user_orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID（主键，自增）',
  `order_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付方式',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单备注',
  `consignee` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `address` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货地址',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（关联sys_user表）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_number`(`order_number` ASC) USING BTREE,
  INDEX `fk_user_orders_username`(`user_name` ASC) USING BTREE,
  CONSTRAINT `fk_user_orders_username` FOREIGN KEY (`user_name`) REFERENCES `sys_user` (`username`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_orders
-- ----------------------------
INSERT INTO `user_orders` VALUES (1, 'ORDb42c0cac3cd64b87', '2025-06-03 08:35:48', 'commod', 'velit ad officia Excepteur', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 5.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (2, 'ORD793ceb966bb04c8e', '2025-06-03 08:45:57', 'wechat', '1234', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (3, 'ORDb065f0b927a14e99', '2025-06-03 08:49:39', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (4, 'ORD8bd977257c8a449f', '2025-06-03 08:51:36', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (5, 'ORD0a4385acc3a4493e', '2025-06-03 08:55:48', 'wechat', '1223', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (6, 'ORD21816e431f48418d', '2025-06-03 08:56:34', 'wechat', '12234', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (7, 'ORDa6f0577cca684f18', '2025-06-03 08:56:43', 'wechat', '12234', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (8, 'ORD97d6020eba2d4c9e', '2025-06-03 08:58:46', 'wechat', '12234', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (9, 'ORD66454bfda0264b14', '2025-06-03 08:59:35', 'wechat', '23456', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (10, 'ORD68b28990fd954b76', '2025-06-03 09:03:41', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (11, 'ORDb2c57cd67aba4bcf', '2025-06-03 09:11:23', 'wechat', '123', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (12, 'ORDcadbe6e3bba54d70', '2025-06-03 09:13:10', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (13, 'ORDa0fefc52493b46c7', '2025-06-03 09:22:22', 'wechat', '88888', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 5.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (14, 'ORD547f50ba26f74fd5', '2025-06-03 09:24:18', 'wechat', '44444', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (15, 'ORD107affaaea4e4fe1', '2025-06-03 09:25:07', 'wechat', '999', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (16, 'ORD09e001bbbbac45e9', '2025-06-03 09:30:03', 'wechat', '88888', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (17, 'ORDb420e4a686d44711', '2025-06-03 09:30:31', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (18, 'ORD9cd725be99df4ae2', '2025-06-03 09:35:05', 'wechat', '8900', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (19, 'ORDc1e8418e6af147fe', '2025-06-03 09:55:30', 'wechat', '999', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (20, 'ORD8f971301109b4765', '2025-06-03 14:53:19', 'wechat', '122', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 4.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (21, 'ORD2a3199497b704cdb', '2025-06-03 16:23:51', 'wechat', '99999999', 'uu', '15973773352', '湖南省益阳市沅江市667878989', 3.50, 'lihaoya');
INSERT INTO `user_orders` VALUES (22, 'ORD50e820edcfdb4c65', '2025-06-03 19:05:54', 'wechat', '9999', 'uu', '15973773352', '湖南省益阳市沅江市66787898', 7.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (23, 'ORD7546fa24cdda47de', '2025-06-05 14:30:24', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市6678789', 8.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (24, 'ORDb4a5ebe3986c49b4', '2025-06-10 13:40:47', 'wechat', '12334', 'uu', '15973773352', '湖南省益阳市沅江市6678788\n\n', 7.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (25, 'ORD83633d031e5245f7', '2025-06-10 16:06:35', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市6678788\n\n', 7.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (26, 'ORD03e9b43a185e4ef5', '2025-06-15 08:10:00', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市6678788\n\n', 7.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (27, 'ORD563e614981364c39', '2025-06-15 09:39:00', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市6678788\n\n', 7.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (28, 'ORDb9adcc29dc3a4126', '2025-06-16 08:44:39', 'wechat', '', 'uu', '15973773352', '湖南省益阳市沅江市6678788\n\n', 7.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (29, 'ORD19211c4f6f164712', '2025-06-17 10:26:40', 'ei', 'aliquip', 'lihaoya4', '15973773352', '4444湖南省 / 张家界市 / 永定区44449999999999998', 2.00, 'lihaoya');
INSERT INTO `user_orders` VALUES (30, 'ORD101956f5a50c417f', '2025-06-17 10:32:05', 'wechat', '', 'lihaoya4', '15973773352', '4444湖南省 / 张家界市 / 永定区44449999999999998', 59.80, 'lihaoya');
INSERT INTO `user_orders` VALUES (31, 'ORD03ee5a94f4014783', '2025-06-17 10:32:40', 'wechat', '', 'lihaoyas', '15973773352', '4444湖南省 / 张家界市 / 永定区44449999999880', 29.90, 'lihaoyas');
INSERT INTO `user_orders` VALUES (32, 'ORD95e7d7b3aa424721', '2025-06-17 10:36:01', 'wechat', '', 'lihaoya4', '15973773352', '4444湖南省 / 张家界市 / 永定区44449999999999998', 59.80, 'lihaoya');
INSERT INTO `user_orders` VALUES (33, 'ORDe44b30aad7a44296', '2025-06-17 10:42:32', 'wechat', '', 'lihaoya4', '15973773352', '4444湖南省 / 张家界市 / 永定区44449999999999998', 59.80, 'lihaoya');
INSERT INTO `user_orders` VALUES (34, 'ORD6e5ce76a366a48e0', '2025-06-17 10:46:15', 'wechat', '', 'lihaoya4', '15973773352', '4444湖南省 / 张家界市 / 永定区44449999999999998', 59.80, 'lihaoya');
INSERT INTO `user_orders` VALUES (35, 'ORDd39a030a175f4e64', '2025-06-17 10:49:55', 'wechat', '', 'lihaoya4', '15973773352', '4444湖南省 / 张家界市 / 永定区44449999999999998', 119.60, 'lihaoya');
INSERT INTO `user_orders` VALUES (36, 'ORD1b01be0c776a4587', '2025-06-17 14:34:03', 'wechat', '', 'lihaoya4', '15973773352', '4444湖南省 / 张家界市 / 永定区44449999999999998', 7.00, 'lihaoya');

SET FOREIGN_KEY_CHECKS = 1;
