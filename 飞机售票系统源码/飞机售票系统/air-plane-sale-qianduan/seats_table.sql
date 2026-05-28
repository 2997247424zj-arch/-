-- 座位表建表语句
-- 用于存储每个航班的座位信息，包括座位号、舱位等级、价格、余票数量等

CREATE TABLE seats
(
    id               INT AUTO_INCREMENT PRIMARY KEY COMMENT '座位ID',
    flight_id        INT NOT NULL COMMENT '航班ID，关联flights表',
    seat_number      VARCHAR(10) NOT NULL COMMENT '座位号，如：1A, 1B, 2C等',
    cabin_class      ENUM('头等舱', '商务舱', '经济舱') NOT NULL COMMENT '舱位等级',
    row_number       INT NOT NULL COMMENT '行号（排号）',
    seat_position    VARCHAR(2) NOT NULL COMMENT '座位位置，如：A, B, C, D, E, F等',
    price            DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '座位价格（单位：元），不同舱位和位置可能有不同价格',
    available_count  INT NOT NULL DEFAULT 1 COMMENT '余票数量，通常为1（每个座位只能被一人占用），但某些座位可能允许多人共享',
    status           ENUM('available', 'occupied', 'reserved', 'maintenance') DEFAULT 'available' COMMENT '座位状态：available-可用, occupied-已占用, reserved-已预订, maintenance-维护中',
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 唯一约束：同一航班的座位号必须唯一
    CONSTRAINT uk_flight_seat_number UNIQUE (flight_id, seat_number),
    
    -- 外键约束：关联到flights表
    CONSTRAINT fk_seats_flight FOREIGN KEY (flight_id) REFERENCES flights (id) ON DELETE CASCADE,
    
    -- 索引：提高查询性能
    INDEX idx_flight_id (flight_id),
    INDEX idx_cabin_class (cabin_class),
    INDEX idx_status (status),
    INDEX idx_row_number (row_number),
    INDEX idx_flight_cabin_status (flight_id, cabin_class, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位信息表';

-- 说明：
-- 1. seat_number: 座位号格式为"行号+位置"，如"1A"表示第1排A座
-- 2. cabin_class: 舱位等级，根据aircraft_types表的seat_layout字段来确定
-- 3. price: 座位价格，可以根据舱位等级和位置设置不同价格
-- 4. available_count: 余票数量，通常为1，但某些特殊座位（如家庭座位）可能允许多人共享
-- 5. status: 座位状态，用于控制座位的可用性
-- 6. 座位布局应根据aircraft_types表的seat_layout JSON字段来生成：
--    - 从seat_layout中获取各舱位的座位数量
--    - 根据数量合理分配行号和位置
--    - 例如：商务舱6个座位，可以安排为2排，每排3个座位（A, B, C或A, B, D, E等）
--    - 例如：经济舱186个座位，可以安排为31排，每排6个座位（A, B, C, D, E, F）


















