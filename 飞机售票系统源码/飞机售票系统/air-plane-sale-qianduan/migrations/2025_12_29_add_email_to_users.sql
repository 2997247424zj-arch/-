-- Migration: 添加 email 列到 users 表
-- 说明：为了解决注册时查询 email 导致的 SQL 错误（Unknown column 'email'），
-- 请在应用数据库（flight）上执行此 SQL。

-- 1) 增加 email 字段，允许 NULL，长度 100，唯一约束
ALTER TABLE users
  ADD COLUMN email VARCHAR(100) DEFAULT NULL;

-- 2) 为 email 添加唯一索引（如果已有重复数据会失败，请先清理重复数据）
CREATE UNIQUE INDEX idx_users_email_unique ON users (email);

-- 说明：
-- - 若你的数据库用户名/权限或表前缀不同，请根据实际情况调整表名。
-- - 在执行前，建议先备份 users 表： CREATE TABLE users_backup AS SELECT * FROM users;
-- - 若执行 CREATE UNIQUE INDEX 报错（存在重复 email），请先查找重复记录：
--     SELECT email, COUNT(*) c FROM users GROUP BY email HAVING c > 1;
--   并清理后再创建唯一索引。


