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
public class ReviewSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureProductReviewTable();
        ensureReviewLikeTable();
        seedSampleReviewsIfNeeded();
    }

    private void ensureProductReviewTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS `product_review` (
              `id` bigint NOT NULL AUTO_INCREMENT,
              `product_id` bigint NOT NULL,
              `user_id` bigint NOT NULL,
              `order_id` bigint DEFAULT NULL,
              `rating` int NOT NULL,
              `content` varchar(1000) NOT NULL,
              `images` varchar(1000) DEFAULT NULL,
              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              PRIMARY KEY (`id`),
              KEY `idx_product_id` (`product_id`),
              KEY `idx_user_id` (`user_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表'
        """);
    }

    private void ensureReviewLikeTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS `review_like` (
              `id` bigint NOT NULL AUTO_INCREMENT,
              `review_id` bigint NOT NULL,
              `user_id` bigint NOT NULL,
              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
              PRIMARY KEY (`id`),
              UNIQUE KEY `uk_review_user` (`review_id`, `user_id`),
              KEY `idx_user_id` (`user_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价点赞表'
        """);
    }

    private void seedSampleReviewsIfNeeded() {
        Integer reviewCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `product_review`", Integer.class);
        if (reviewCount != null && reviewCount > 0) {
            return;
        }

        Integer productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `product`", Integer.class);
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `user`", Integer.class);
        if (productCount == null || productCount == 0 || userCount == null || userCount == 0) {
            return;
        }

        jdbcTemplate.update(
            "INSERT INTO `product_review` (`product_id`, `user_id`, `order_id`, `rating`, `content`, `images`) VALUES (?, ?, ?, ?, ?, ?)",
            1L, 1L, null, 5, "手机做工很扎实，系统流畅，拍照表现也稳定，日常和出差都够用。", null
        );
        jdbcTemplate.update(
            "INSERT INTO `product_review` (`product_id`, `user_id`, `order_id`, `rating`, `content`, `images`) VALUES (?, ?, ?, ?, ?, ?)",
            3L, 2L, null, 4, "性能很强，屏幕和键盘体验都不错，编译项目和写文档都很顺手。", null
        );
        jdbcTemplate.update(
            "INSERT INTO `product_review` (`product_id`, `user_id`, `order_id`, `rating`, `content`, `images`) VALUES (?, ?, ?, ?, ?, ?)",
            8L, 5L, null, 5, "降噪效果明显，通勤时提升非常大，佩戴几个小时也没有明显压头感。", null
        );

        log.info("Seeded sample product reviews");
    }
}
