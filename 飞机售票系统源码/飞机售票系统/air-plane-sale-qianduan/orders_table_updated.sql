-- ============================================
-- 订单表（更新版）
-- 添加客户姓名、航线、起飞时间字段
-- ============================================

CREATE TABLE orders (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  passenger_id  INT NOT NULL COMMENT '乘客ID（关联users表，实现数据隔离）',
  order_no      VARCHAR(40) NOT NULL COMMENT '订单号',
  passenger_name VARCHAR(50) NULL COMMENT '客户姓名（冗余字段，便于查询，从users.real_name同步）',
  route         VARCHAR(100) NULL COMMENT '航线（格式：出发地 → 目的地，如：北京 → 上海）',
  departure_time DATETIME NULL COMMENT '起飞时间',
  total_amount  DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  status        ENUM('created','paid','ticketed','cancelled','refunded') DEFAULT 'created' COMMENT '订单状态',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_passenger (passenger_id),
  KEY idx_passenger_name (passenger_name),
  KEY idx_route (route),
  KEY idx_departure_time (departure_time),
  KEY idx_status (status),
  KEY idx_created_at (created_at),
  
  CONSTRAINT fk_order_user FOREIGN KEY (passenger_id) REFERENCES users(id) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE
) 
ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='订单主表（乘客隔离，包含客户姓名、航线、起飞时间）';

-- ============================================
-- 数据说明：
-- 1. passenger_name: 冗余字段，从users表的real_name同步，便于查询和显示
-- 2. route: 从tickets关联flights表获取，格式为"出发地 → 目的地"
-- 3. departure_time: 从tickets关联flights表的sched_dep_time获取
-- 
-- 数据同步建议：
-- 创建订单时，从users表获取real_name填充passenger_name
-- 创建tickets后，从flights表获取航线信息和起飞时间，更新orders表
-- ============================================


















