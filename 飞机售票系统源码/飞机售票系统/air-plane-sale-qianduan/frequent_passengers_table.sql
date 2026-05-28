-- ============================================
-- 常用乘客表（添加乘车人功能）
-- 用于存储用户添加的常用乘客信息，实现数据隔离
-- ============================================

-- 常用乘客信息表
CREATE TABLE frequent_passengers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  user_id INT NOT NULL COMMENT '所属用户ID（关联users表，实现数据隔离）',
  name VARCHAR(50) NOT NULL COMMENT '乘客姓名',
  id_card VARCHAR(18) NOT NULL COMMENT '身份证号（18位）',
  relationship VARCHAR(20) NULL COMMENT '与用户的关系（配偶、子女、父母、兄弟姐妹、朋友、同事、其他）',
  phone VARCHAR(15) NULL COMMENT '联系电话',
  remarks TEXT NULL COMMENT '备注信息',
  is_default TINYINT(1) DEFAULT 0 COMMENT '是否设为默认（0-否，1-是）',
  status ENUM('active', 'deleted') DEFAULT 'active' COMMENT '状态（active-有效，deleted-已删除）',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  -- 索引
  INDEX idx_user_id (user_id) COMMENT '用户ID索引（用于快速查询该用户的常用乘客）',
  INDEX idx_user_status (user_id, status) COMMENT '用户ID和状态联合索引',
  INDEX idx_id_card (id_card) COMMENT '身份证号索引（用于验证唯一性）',
  
  -- 外键约束
  CONSTRAINT fk_frequent_passenger_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  
  -- 唯一约束：同一用户下，身份证号不能重复（同一用户不能添加重复的常用乘客）
  UNIQUE KEY uk_user_id_card (user_id, id_card) COMMENT '同一用户下身份证号唯一'
) 
ENGINE=InnoDB 
DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_unicode_ci
COMMENT='常用乘客信息表（每个用户的数据完全隔离）';

-- ============================================
-- 数据隔离说明：
-- 1. 通过 user_id 字段关联到 users 表
-- 2. 查询时必须加上 WHERE user_id = ? 条件
-- 3. 每个用户只能看到和管理自己的常用乘客
-- 4. 同一用户下，身份证号不能重复（防止重复添加）
-- ============================================

-- ============================================
-- 使用示例：
-- ============================================

-- 1. 添加常用乘客（需要传入当前登录用户的 user_id）
-- INSERT INTO frequent_passengers (user_id, name, id_card, relationship, phone, remarks)
-- VALUES (1, '张三', '110101199001011234', '朋友', '13800138000', '经常一起出行');

-- 2. 查询当前用户的常用乘客列表（必须加上 user_id 条件）
-- SELECT * FROM frequent_passengers 
-- WHERE user_id = 1 AND status = 'active'
-- ORDER BY created_at DESC;

-- 3. 更新常用乘客信息（必须验证 user_id 匹配）
-- UPDATE frequent_passengers 
-- SET name = '李四', phone = '13900139000'
-- WHERE id = 1 AND user_id = 1;

-- 4. 删除常用乘客（软删除，设置 status = 'deleted'）
-- UPDATE frequent_passengers 
-- SET status = 'deleted'
-- WHERE id = 1 AND user_id = 1;

-- 5. 设置默认常用乘客
-- UPDATE frequent_passengers 
-- SET is_default = 1
-- WHERE id = 1 AND user_id = 1;

-- ============================================
-- 注意事项：
-- ============================================
-- 1. 所有查询和操作都必须加上 user_id 条件，确保数据隔离
-- 2. 后端API必须验证当前登录用户的身份，防止越权访问
-- 3. 身份证号在同一用户下唯一，但不同用户可以有相同的身份证号
-- 4. 使用软删除（status字段）而不是物理删除，便于数据恢复和审计
-- ============================================

