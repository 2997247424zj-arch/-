-- 积分与优惠券建表语句

-- 5. 积分变动（乘客隔离）
CREATE TABLE IF NOT EXISTS loyalty_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    passenger_id INT NOT NULL,
    points INT NOT NULL,
    change_type VARCHAR(255),
    ref_order_id BIGINT NULL,
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_passenger_time (passenger_id, created_at),
    CONSTRAINT fk_lp_user FOREIGN KEY (passenger_id) REFERENCES users(id),
    CONSTRAINT fk_lp_order FOREIGN KEY (ref_order_id) REFERENCES orders(id)
) COMMENT='积分变动（乘客隔离）';

-- 6. 优惠券（乘客隔离或通用）
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    passenger_id INT NULL, -- NULL 表示通用券；非空表示专属且隔离
    discount_type VARCHAR(255),
    discount_value DECIMAL(10,2) NOT NULL,
    min_spend DECIMAL(10,2) DEFAULT 0,
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    status VARCHAR(255),
    name VARCHAR(255),
    description VARCHAR(500),
    UNIQUE KEY uk_code (code),
    KEY idx_passenger (passenger_id),
    CONSTRAINT fk_coupon_user FOREIGN KEY (passenger_id) REFERENCES users(id)
) COMMENT='优惠券（乘客隔离或通用）';

