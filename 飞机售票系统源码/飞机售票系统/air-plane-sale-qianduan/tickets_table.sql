-- ============================================
-- 机票表（Tickets Table）
-- 用于存储每个乘客的机票信息
-- 一个订单可能包含多张机票（多个乘客）
-- ============================================

CREATE TABLE tickets (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '机票ID',
    
    -- 订单关联（一个订单可能有多张机票）
    order_id            BIGINT NOT NULL COMMENT '订单ID，关联orders(id)',
    order_no            VARCHAR(40) NOT NULL COMMENT '订单号，关联orders(order_no)，冗余字段便于查询',
    
    -- 乘客信息（数据隔离）
    passenger_id        INT NOT NULL COMMENT '乘客ID（关联users表，实现数据隔离）',
    passenger_name      VARCHAR(50) NOT NULL COMMENT '乘客姓名（冗余字段，便于查询）',
    id_card             VARCHAR(20) NOT NULL COMMENT '身份证号',
    phone               VARCHAR(15) NULL COMMENT '联系电话',
    
    -- 航班信息
    flight_id           INT NOT NULL COMMENT '航班ID，关联flights(id)',
    flight_no           VARCHAR(20) NOT NULL COMMENT '航班号',
    origin_airport      VARCHAR(50) NOT NULL COMMENT '出发机场',
    dest_airport        VARCHAR(50) NOT NULL COMMENT '到达机场',
    route               VARCHAR(100) NULL COMMENT '航线（格式：出发地 → 目的地，如：北京 → 上海）',
    departure_time      DATETIME NOT NULL COMMENT '起飞时间',
    arrival_time        DATETIME NOT NULL COMMENT '到达时间',
    
    -- 座位信息
    seat_id             INT NULL COMMENT '座位ID，关联seats(id)',
    seat_number         VARCHAR(10) NULL COMMENT '座位号，如：10A, 12B等',
    seat_class          VARCHAR(20) NULL COMMENT '舱位等级（economy-经济舱, business-商务舱, first-头等舱）',
    
    -- 价格信息
    base_price          DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '基础票价',
    seat_fee            DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '座位选择费（如有）',
    total_price         DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '机票总价（基础票价 + 座位费）',
    
    -- 机票号（唯一标识）
    ticket_no            VARCHAR(50) NOT NULL COMMENT '机票号（唯一标识，格式：如TKT+时间戳+随机数）',
    
    -- 状态
    status              VARCHAR(20) NOT NULL DEFAULT 'issued' COMMENT '机票状态：issued-已出票, used-已使用, cancelled-已取消, refunded-已退款, changed-已改签',
    
    -- 时间戳
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 唯一约束
    UNIQUE KEY uk_ticket_no (ticket_no),
    
    -- 外键约束
    CONSTRAINT fk_ticket_order FOREIGN KEY (order_id) REFERENCES orders(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_user FOREIGN KEY (passenger_id) REFERENCES users(id) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_flight FOREIGN KEY (flight_id) REFERENCES flights(id) 
        ON DELETE RESTRICT 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ticket_seat FOREIGN KEY (seat_id) REFERENCES seats(id) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    
    -- 索引
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no),
    INDEX idx_passenger_id (passenger_id),
    INDEX idx_passenger_name (passenger_name),
    INDEX idx_id_card (id_card),
    INDEX idx_flight_id (flight_id),
    INDEX idx_flight_no (flight_no),
    INDEX idx_departure_time (departure_time),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_ticket_no (ticket_no),
    INDEX idx_passenger_flight (passenger_id, flight_id),
    INDEX idx_order_passenger (order_id, passenger_id)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci
  COMMENT='机票表（每个乘客一张机票，支持一个订单多张机票）'
  ROW_FORMAT=DYNAMIC;

-- ============================================
-- 数据说明：
-- 1. 一个订单（orders）可以包含多张机票（tickets），每个乘客一张
-- 2. passenger_name 和 id_card 是冗余字段，便于查询和显示，无需关联其他表
-- 3. ticket_no 是唯一标识，建议格式：TKT + 时间戳 + 随机数，如：TKT20250125123456789012
-- 4. seat_id 可以为空，如果乘客未选择座位或座位被释放
-- 5. status 状态说明：
--    - issued: 已出票（默认状态）
--    - used: 已使用（航班已起飞或已完成）
--    - cancelled: 已取消
--    - refunded: 已退款
--    - changed: 已改签（原机票状态，新机票会创建新记录）
-- 
-- 数据同步建议：
-- 1. 创建订单时，为每个乘客创建一张机票记录
-- 2. 机票号（ticket_no）在创建时自动生成，确保唯一性
-- 3. 如果乘客选择了座位，需要关联seats表并更新seat_id和seat_number
-- 4. 订单状态更新为'ticketed'时，所有关联机票状态应为'issued'
-- ============================================

