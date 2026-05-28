package com.example.ecommerceback.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureColumn("avatar", "ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) COMMENT '头像地址' AFTER `phone`");
        ensureColumn("status", "ALTER TABLE `user` ADD COLUMN `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用' AFTER `avatar`");
        ensureColumn("role", "ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色：admin/user' AFTER `status`");
    }

    private void ensureColumn(String columnName, String alterSql) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = ?",
            Integer.class,
            columnName
        );

        if (count == null || count == 0) {
            jdbcTemplate.execute(alterSql);
            log.info("Added missing user.{} column", columnName);
        }
    }
}
