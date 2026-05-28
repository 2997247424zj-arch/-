-- ============================================
-- 行李管理表（乘客隔离）
-- 用于存储普通乘客和运营人员的行李管理信息
-- 通过 passenger_id 实现数据隔离，普通乘客只能看到自己的行李数据
-- ============================================

CREATE TABLE baggage_management (
    id                  BIGINT AUTO_INCREMENT COMMENT '主键'
        PRIMARY KEY,
    
    -- 行李编号/标识号，用于前端展示
    baggage_no          VARCHAR(40)                              NOT NULL COMMENT '行李编号/标识号，用于前端展示',
    
    -- 数据隔离字段
    passenger_id        INT                                       NOT NULL COMMENT '用户ID/乘客ID，关联users(id)，实现数据隔离',
    passenger_name      VARCHAR(50)                              NULL COMMENT '乘客姓名（冗余展示）',
    
    -- 订单和航班关联
    orderno             VARCHAR(40)                              NOT NULL COMMENT '订单号，关联orders(order_no)',
    flight_no           VARCHAR(20)                              NOT NULL COMMENT '航班号',
    route               VARCHAR(100)                             NOT NULL COMMENT '航线（格式：出发地 → 目的地）',
    departure_time      DATETIME                                 NOT NULL COMMENT '起飞时间',
    arrival_time_flight  DATETIME                                 NULL COMMENT '航班到达时间',
    
    -- 行李基本信息
    baggage_type        VARCHAR(50)                              NOT NULL COMMENT '行李类型（托运行李、随身行李、超规行李、特殊行李等）',
    baggage_count       INT           DEFAULT 1                   NOT NULL COMMENT '行李数量',
    total_weight        DECIMAL(10, 2)                            NULL COMMENT '总重量（公斤）',
    weight_limit        DECIMAL(10, 2)                            NULL COMMENT '重量限制（公斤）',
    dimensions          VARCHAR(100)                              NULL COMMENT '尺寸（长x宽x高，单位：厘米，如：100x60x40）',
    
    -- 行李状态
    status              VARCHAR(50)   DEFAULT 'registered'       NOT NULL COMMENT '行李状态（registered-已登记、checked_in-已托运、in_transit-运输中、arrived-已到达、delivered-已提取、lost-丢失、damaged-损坏、delayed-延误）',
    
    -- 时间信息
    registered_time     DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '登记时间',
    checked_in_time     DATETIME                                 NULL COMMENT '托运时间',
    arrival_time_baggage DATETIME                                NULL COMMENT '行李到达时间',
    delivered_time      DATETIME                                 NULL COMMENT '提取时间',
    
    -- 费用信息
    baggage_fee         DECIMAL(10, 2) DEFAULT 0.00               NULL COMMENT '行李费用',
    excess_fee          DECIMAL(10, 2) DEFAULT 0.00               NULL COMMENT '超重/超规费用',
    
    -- 备注信息
    description         TEXT                                     NULL COMMENT '行李描述（颜色、特征等，便于识别）',
    remark              TEXT                                     NULL COMMENT '备注信息',
    
    -- 运营处理信息
    processed_by        INT                                       NULL COMMENT '运营处理人ID，关联users(id)',
    processed_at        DATETIME                                 NULL COMMENT '处理时间',
    operator_remark     TEXT                                     NULL COMMENT '运营备注',
    
    -- 索引
    CONSTRAINT uk_baggage_no
        UNIQUE (baggage_no),
    CONSTRAINT fk_bm_orders
        FOREIGN KEY (orderno) REFERENCES orders (order_no),
    CONSTRAINT fk_bm_passengers
        FOREIGN KEY (passenger_id) REFERENCES users (id),
    CONSTRAINT fk_bm_processeds
        FOREIGN KEY (processed_by) REFERENCES users (id)
)
    COMMENT '行李管理表（乘客隔离）' COLLATE = utf8mb4_unicode_ci;

-- 创建索引以优化查询性能
CREATE INDEX idx_passenger
    ON baggage_management (passenger_id);

CREATE INDEX idx_order
    ON baggage_management (orderno);

CREATE INDEX idx_flight
    ON baggage_management (flight_no);

CREATE INDEX idx_status
    ON baggage_management (status);

CREATE INDEX idx_passenger_status
    ON baggage_management (passenger_id, status);

CREATE INDEX idx_departure_time
    ON baggage_management (departure_time);

CREATE INDEX idx_registered_time
    ON baggage_management (registered_time);

CREATE INDEX idx_processed_by
    ON baggage_management (processed_by);

-- ============================================
-- 数据隔离说明：
-- ============================================
-- 1. passenger_id: 必填，用于数据隔离，关联users表
--    普通乘客查询时：WHERE passenger_id = 当前用户ID
--    运营人员查询时：可以查询所有数据（不限制passenger_id）
-- 
-- 2. baggage_no: 行李编号，唯一标识，用于前端展示和查询
-- 
-- 3. 状态说明：
--    - registered: 已登记（乘客已登记行李信息）
--    - checked_in: 已托运（行李已办理托运手续）
--    - in_transit: 运输中（行李正在运输过程中）
--    - arrived: 已到达（行李已到达目的地）
--    - delivered: 已提取（乘客已提取行李）
--    - lost: 丢失（行李丢失）
--    - damaged: 损坏（行李损坏）
--    - delayed: 延误（行李延误）
-- 
-- 4. 数据隔离实现：
--    普通乘客：只能看到 passenger_id = 自己ID 的记录
--    运营人员：可以看到所有记录，可以处理所有行李
-- 
-- 5. 运营处理：
--    processed_by: 运营人员处理行李时，记录处理人ID
--    processed_at: 记录处理时间
--    operator_remark: 运营人员可以添加备注信息
-- ============================================

-- ============================================
-- 使用示例：
-- ============================================

-- 1. 普通乘客查询自己的行李列表（必须加上 passenger_id 条件）
-- SELECT * FROM baggage_management 
-- WHERE passenger_id = 1 
-- ORDER BY registered_time DESC;

-- 2. 普通乘客按状态查询行李
-- SELECT * FROM baggage_management 
-- WHERE passenger_id = 1 AND status = 'checked_in'
-- ORDER BY checked_in_time DESC;

-- 3. 运营人员查询所有行李（不限制 passenger_id）
-- SELECT * FROM baggage_management 
-- WHERE status = 'registered'
-- ORDER BY registered_time DESC;

-- 4. 运营人员处理行李（更新状态和处理人信息）
-- UPDATE baggage_management 
-- SET status = 'checked_in',
--     checked_in_time = NOW(),
--     processed_by = 2,  -- 运营人员ID
--     processed_at = NOW(),
--     operator_remark = '行李已成功托运'
-- WHERE id = 1;

-- 5. 添加新行李记录（乘客登记）
-- INSERT INTO baggage_management (
--     baggage_no, passenger_id, passenger_name, orderno, 
--     flight_no, route, departure_time, baggage_type, 
--     baggage_count, total_weight, description, status
-- ) VALUES (
--     'BG20250101001', 1, '张三', 'ORD20250101001',
--     'CA1234', '北京 → 上海', '2025-01-01 10:00:00',
--     '托运行李', 2, 25.5, '黑色行李箱，带红色标签', 'registered'
-- );

-- 6. 查询特定订单的行李
-- SELECT * FROM baggage_management 
-- WHERE orderno = 'ORD20250101001' AND passenger_id = 1;

-- ============================================
-- 注意事项：
-- ============================================
-- 1. 所有普通乘客的查询和操作都必须加上 passenger_id 条件，确保数据隔离
-- 2. 后端API必须验证当前登录用户的身份，防止越权访问
-- 3. 运营人员可以查看和处理所有行李，但需要记录处理人信息
-- 4. baggage_no 必须唯一，建议使用规则生成（如：BG + 日期 + 序号）
-- 5. 时间字段用于跟踪行李的各个阶段，便于追溯
-- 6. 费用信息可以用于统计和结算
-- ============================================

