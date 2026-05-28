-- ============================================
-- 重点旅客预约表（乘客隔离）
-- 用于存储普通乘客的重点旅客预约申请
-- ============================================

CREATE TABLE special_service_requests (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  
  -- 数据隔离字段
  passenger_id INT NOT NULL COMMENT '乘客ID（关联users表，实现数据隔离）',
  frequent_passenger_id BIGINT NULL COMMENT '常用乘客ID（关联frequent_passengers表，可为空表示本人）',
  
  -- 订单关联
  order_id BIGINT NOT NULL COMMENT '订单ID（关联orders表）',
  
  -- 联系信息
  country_code VARCHAR(10) DEFAULT '+86' COMMENT '国家代码',
  phone VARCHAR(15) NOT NULL COMMENT '联系电话',
  
  -- 旅客信息
  passenger_type VARCHAR(50) NOT NULL COMMENT '旅客类型（无陪伴年长旅客、无陪伴孕妇旅客、视觉障碍旅客、听觉障碍旅客、轮椅行动障碍旅客、担架行动障碍旅客、携带导盲犬旅客等）',
  
  -- 机场信息
  departure_airport VARCHAR(100) NOT NULL COMMENT '出发机场',
  arrival_airport VARCHAR(100) NOT NULL COMMENT '到达机场',
  
  -- 服务需求（使用JSON存储，便于扩展）
  entry_services JSON NULL COMMENT '进站服务需求（自备器械、优先进站、提供轮椅、提供担架等）',
  exit_services JSON NULL COMMENT '出站服务需求（自备器械、便利出站、提供轮椅、提供担架等）',
  
  -- 描述信息
  description TEXT NULL COMMENT '情况描述（非必填）',
  
  -- 状态字段（字符型）
  status VARCHAR(20) DEFAULT 'pending' COMMENT '申请状态（pending-待处理、approved-已批准、processing-处理中、completed-已完成、rejected-已拒绝、cancelled-已取消）',
  
  -- 时间戳
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 索引
  INDEX idx_passenger (passenger_id) COMMENT '乘客ID索引（用于快速查询该用户的预约）',
  INDEX idx_frequent_passenger (frequent_passenger_id) COMMENT '常用乘客ID索引',
  INDEX idx_order (order_id) COMMENT '订单ID索引',
  INDEX idx_status (status) COMMENT '状态索引（用于快速查询不同状态的申请）',
  INDEX idx_passenger_status (passenger_id, status) COMMENT '乘客ID和状态联合索引',
  INDEX idx_created_at (created_at) COMMENT '创建时间索引',
  
  -- 外键约束
  CONSTRAINT fk_ssr_user 
    FOREIGN KEY (passenger_id) REFERENCES users(id)
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  
  CONSTRAINT fk_ssr_frequent_passenger 
    FOREIGN KEY (frequent_passenger_id) REFERENCES frequent_passengers(id)
    ON DELETE SET NULL 
    ON UPDATE CASCADE,
  
  CONSTRAINT fk_ssr_order 
    FOREIGN KEY (order_id) REFERENCES orders(id)
    ON DELETE CASCADE 
    ON UPDATE CASCADE
  
) 
ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='重点旅客预约表（乘客隔离，包含常用乘客关联）';

-- ============================================
-- 数据说明：
-- 1. passenger_id: 必填，用于数据隔离，关联users表
-- 2. frequent_passenger_id: 可选，如果预约的是常用乘客，则关联frequent_passengers表；如果为空，表示预约的是本人
-- 3. order_id: 必填，关联订单表
-- 4. entry_services: JSON格式，存储进站服务需求，例如：
--    {"selfEquipment": true, "priorityEntry": false, "wheelchair": true, "stretcher": false}
-- 5. exit_services: JSON格式，存储出站服务需求，例如：
--    {"selfEquipment": false, "convenientExit": true, "wheelchair": true, "stretcher": false}
-- 6. status: 状态字段，使用VARCHAR类型，支持的状态值：
--    - pending: 待处理（默认）
--    - approved: 已批准
--    - processing: 处理中
--    - completed: 已完成
--    - rejected: 已拒绝
--    - cancelled: 已取消
-- ============================================












