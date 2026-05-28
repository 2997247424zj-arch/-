-- ============================================
-- 添加软删除字段到机票表（Tickets Table）
-- 为实现软删除功能，添加 deleted_at 字段
-- ============================================

-- 添加 deleted_at 字段到 tickets 表
ALTER TABLE tickets 
ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间，NULL表示未删除';

-- 更新索引以包含 deleted_at 字段（可选，提高查询性能）
CREATE INDEX idx_tickets_passenger_deleted ON tickets (passenger_id, deleted_at);

-- 为确保查询性能，也可以创建单独的索引
CREATE INDEX idx_tickets_deleted_at ON tickets (deleted_at);

-- ============================================
-- 使用说明：
-- 1. 软删除操作：UPDATE tickets SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?;
-- 2. 查询未删除记录：SELECT * FROM tickets WHERE deleted_at IS NULL;
-- 3. 恢复已删除记录：UPDATE tickets SET deleted_at = NULL WHERE id = ?;
-- ============================================