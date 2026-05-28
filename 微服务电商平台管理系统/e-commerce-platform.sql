/*
 SQL dump for e-commerce-platform
 Updated: 2026-04-25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Cart ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `product_id` bigint NOT NULL COMMENT 'Product ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT 'Quantity',
  `is_selected` tinyint NOT NULL DEFAULT 1 COMMENT 'Selected: 1 yes, 0 no',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id`, `product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Cart table';

DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Order ID',
  `order_no` varchar(32) NOT NULL COMMENT 'Order number',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `address_id` bigint NOT NULL COMMENT 'Address ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT 'Total amount',
  `pay_amount` decimal(10,2) NOT NULL COMMENT 'Paid amount',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0 pending, 1 paid, 2 cancelled, 3 completed',
  `pay_time` datetime DEFAULT NULL COMMENT 'Pay time',
  `cancel_time` datetime DEFAULT NULL COMMENT 'Cancel time',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order table';

DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Item ID',
  `order_id` bigint NOT NULL COMMENT 'Order ID',
  `product_id` bigint NOT NULL COMMENT 'Product ID',
  `product_name` varchar(100) NOT NULL COMMENT 'Product name',
  `product_price` decimal(10,2) NOT NULL COMMENT 'Product price',
  `quantity` int NOT NULL COMMENT 'Quantity',
  `total_price` decimal(10,2) NOT NULL COMMENT 'Total price',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Legacy compatibility column',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order item table';

DROP TABLE IF EXISTS `pay_record`;
CREATE TABLE `pay_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Payment record ID',
  `order_no` varchar(32) NOT NULL COMMENT 'Order number',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `pay_amount` decimal(10,2) NOT NULL COMMENT 'Payment amount',
  `pay_type` tinyint NOT NULL COMMENT '1 alipay, 2 wechat',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '0 pending, 1 success, 2 failed',
  `pay_no` varchar(64) DEFAULT NULL COMMENT 'Payment number',
  `pay_time` datetime DEFAULT NULL COMMENT 'Payment time',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_no`(`order_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Payment record table';

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Product ID',
  `category_id` bigint NOT NULL COMMENT 'Category ID',
  `name` varchar(100) NOT NULL COMMENT 'Product name',
  `price` decimal(10,2) NOT NULL COMMENT 'Price',
  `cover_img` varchar(255) NOT NULL COMMENT 'Cover image',
  `description` text COMMENT 'Description',
  `stock` int NOT NULL DEFAULT 0 COMMENT 'Stock',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1 on shelf, 0 off shelf',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `img_url` varchar(255) DEFAULT NULL COMMENT 'Image URL',
  `image` varchar(500) DEFAULT NULL COMMENT 'Image URL compatibility column',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product table';

DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Review ID',
  `product_id` bigint NOT NULL COMMENT 'Product ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `order_id` bigint DEFAULT NULL COMMENT 'Order ID',
  `rating` int NOT NULL COMMENT 'Rating 1-5',
  `content` varchar(1000) NOT NULL COMMENT 'Review content',
  `images` varchar(1000) DEFAULT NULL COMMENT 'Review images',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product review table';

DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Category ID',
  `name` varchar(50) NOT NULL COMMENT 'Category name',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT 'Parent ID',
  `sort` int NOT NULL DEFAULT 0 COMMENT 'Sort',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product category table';

DROP TABLE IF EXISTS `product_stock`;
CREATE TABLE `product_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Stock ID',
  `product_id` bigint NOT NULL COMMENT 'Product ID',
  `stock_num` int NOT NULL DEFAULT 0 COMMENT 'Actual stock',
  `lock_num` int NOT NULL DEFAULT 0 COMMENT 'Locked stock',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_id`(`product_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product stock table';

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  `username` varchar(50) NOT NULL COMMENT 'Username',
  `password` varchar(100) NOT NULL COMMENT 'Password',
  `phone` varchar(11) NOT NULL COMMENT 'Phone',
  `email` varchar(50) DEFAULT NULL COMMENT 'Email',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'Avatar',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1 active, 0 disabled',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  `role` varchar(20) DEFAULT 'user',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User table';

DROP TABLE IF EXISTS `review_like`;
CREATE TABLE `review_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Like ID',
  `review_id` bigint NOT NULL COMMENT 'Review ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_review_user`(`review_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Review like table';

DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Address ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `receiver` varchar(50) NOT NULL COMMENT 'Receiver',
  `phone` varchar(11) NOT NULL COMMENT 'Phone',
  `province` varchar(20) NOT NULL COMMENT 'Province',
  `city` varchar(20) NOT NULL COMMENT 'City',
  `district` varchar(20) NOT NULL COMMENT 'District',
  `detail_address` varchar(255) NOT NULL COMMENT 'Detail address',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '1 default, 0 no',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User address table';

DROP TABLE IF EXISTS `user_points`;
CREATE TABLE `user_points` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Points record ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `points` int NOT NULL COMMENT 'Points',
  `reason` varchar(100) NOT NULL COMMENT 'Reason',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User points table';

INSERT INTO `product_category` (`id`, `name`, `parent_id`, `sort`, `status`) VALUES
(1, 'Electronics', 0, 1, 1),
(2, 'Phones', 1, 1, 1),
(3, 'Computers', 1, 2, 1),
(4, 'Clothing', 0, 2, 1),
(5, 'Men', 4, 1, 1),
(6, 'Women', 4, 2, 1),
(7, 'Audio', 1, 3, 1),
(8, 'Books', 0, 3, 1),
(9, 'Home', 0, 4, 1),
(10, 'Sports', 0, 5, 1),
(11, 'Outdoor', 10, 1, 1),
(12, 'Kitchen', 9, 1, 1),
(13, 'Smart Home', 9, 2, 1);

INSERT INTO `product` (`id`, `category_id`, `name`, `price`, `cover_img`, `description`, `stock`, `status`, `img_url`, `image`) VALUES
(1, 2, 'iPhone 15', 5999.00, 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&auto=format&fit=crop&q=80', 'Apple smartphone with balanced performance and camera.', 100, 1, 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&auto=format&fit=crop&q=80'),
(2, 2, 'Huawei Mate 60', 4999.00, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80', 'Flagship phone focused on communication and imaging.', 80, 1, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80'),
(3, 3, 'MacBook Pro 14', 12999.00, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80', 'Laptop for development, design and heavy office work.', 50, 1, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop&q=80'),
(4, 5, 'Men Casual Jacket', 299.00, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&auto=format&fit=crop&q=80', 'Light jacket for daily commuting.', 200, 1, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800&auto=format&fit=crop&q=80'),
(5, 6, 'Women Knit Dress', 199.00, 'https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=800&auto=format&fit=crop&q=80', 'Comfortable dress with simple silhouette.', 150, 1, 'https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=800&auto=format&fit=crop&q=80'),
(6, 2, 'Xiaomi 14 Ultra', 6499.00, 'https://picsum.photos/seed/xiaomi14ultra/800/800', 'High-end Android phone with strong camera system.', 65, 1, 'https://picsum.photos/seed/xiaomi14ultra/800/800', 'https://picsum.photos/seed/xiaomi14ultra/800/800'),
(7, 3, 'ThinkPad X1 Carbon', 9999.00, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&auto=format&fit=crop&q=80', 'Business laptop with stable keyboard and battery life.', 40, 1, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&auto=format&fit=crop&q=80'),
(8, 7, 'Sony WH-1000XM5', 2399.00, 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800&auto=format&fit=crop&q=80', 'Noise-cancelling headphone for travel and office.', 120, 1, 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=800&auto=format&fit=crop&q=80'),
(9, 7, 'AirPods Pro 2', 1799.00, 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=800&auto=format&fit=crop&q=80', 'Wireless earbuds with active noise cancellation.', 140, 1, 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=800&auto=format&fit=crop&q=80'),
(10, 3, 'Mechanical Keyboard K8', 399.00, 'https://picsum.photos/seed/mechanical-keyboard-k8/800/800', 'Wireless mechanical keyboard for office and coding.', 90, 1, 'https://picsum.photos/seed/mechanical-keyboard-k8/800/800', 'https://picsum.photos/seed/mechanical-keyboard-k8/800/800'),
(11, 10, 'Running Shoes Air Flow', 599.00, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80', 'Breathable running shoes with soft cushioning.', 130, 1, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80'),
(12, 5, 'Essential Hoodie', 259.00, 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&auto=format&fit=crop&q=80', 'Soft hoodie for daily casual wear.', 170, 1, 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=800&auto=format&fit=crop&q=80'),
(13, 8, 'Sapiens', 68.00, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800&auto=format&fit=crop&q=80', 'Popular history book about human civilization.', 220, 1, 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=800&auto=format&fit=crop&q=80'),
(14, 8, 'Deep Learning with Python', 96.00, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=800&auto=format&fit=crop&q=80', 'Technical book for engineers learning deep learning.', 160, 1, 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=800&auto=format&fit=crop&q=80'),
(15, 9, 'Smart Desk Lamp', 299.00, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&auto=format&fit=crop&q=80', 'Adjustable desk lamp for reading and work.', 100, 1, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=800&auto=format&fit=crop&q=80'),
(16, 9, 'Memory Foam Pillow', 129.00, 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=800&auto=format&fit=crop&q=80', 'Supportive pillow for better sleep.', 180, 1, 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=800&auto=format&fit=crop&q=80'),
(17, 10, 'Stainless Steel Bottle', 89.00, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80', 'Reusable bottle for sport and commute.', 260, 1, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80'),
(18, 11, 'Outdoor Hiking Backpack', 399.00, 'https://images.unsplash.com/photo-1622260614153-03223fb72052?w=800&auto=format&fit=crop&q=80', 'Backpack with strong support for hiking trips.', 85, 1, 'https://images.unsplash.com/photo-1622260614153-03223fb72052?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1622260614153-03223fb72052?w=800&auto=format&fit=crop&q=80'),
(19, 11, 'Camping Tent 2P', 599.00, 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800&auto=format&fit=crop&q=80', 'Portable tent for weekend camping.', 55, 1, 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800&auto=format&fit=crop&q=80'),
(20, 12, 'Nonstick Cookware Set', 429.00, 'https://picsum.photos/seed/nonstick-cookware-set/800/800', 'Kitchen cookware set for home cooking.', 75, 1, 'https://picsum.photos/seed/nonstick-cookware-set/800/800', 'https://picsum.photos/seed/nonstick-cookware-set/800/800'),
(21, 13, 'Robot Vacuum Mini', 1699.00, 'https://picsum.photos/seed/robot-vacuum-mini/800/800', 'Compact robot vacuum for apartment cleaning.', 48, 1, 'https://picsum.photos/seed/robot-vacuum-mini/800/800', 'https://picsum.photos/seed/robot-vacuum-mini/800/800'),
(22, 12, 'Espresso Coffee Beans', 89.00, 'https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=800&auto=format&fit=crop&q=80', 'Medium roast beans for espresso and milk coffee.', 210, 1, 'https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=800&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=800&auto=format&fit=crop&q=80'),
(23, 3, 'Monitor 27 QHD', 1499.00, 'https://picsum.photos/seed/monitor-27qhd/800/800', '27-inch monitor for coding and design work.', 70, 1, 'https://picsum.photos/seed/monitor-27qhd/800/800', 'https://picsum.photos/seed/monitor-27qhd/800/800'),
(24, 7, 'Bluetooth Speaker Mini', 299.00, 'https://picsum.photos/seed/bluetooth-speaker-mini/800/800', 'Portable bluetooth speaker for desk and travel.', 145, 1, 'https://picsum.photos/seed/bluetooth-speaker-mini/800/800', 'https://picsum.photos/seed/bluetooth-speaker-mini/800/800');

INSERT INTO `product_stock` (`id`, `product_id`, `stock_num`, `lock_num`) VALUES
(1, 1, 100, 0),
(2, 2, 80, 0),
(3, 3, 50, 0),
(4, 4, 200, 0),
(5, 5, 150, 0),
(6, 6, 65, 0),
(7, 7, 40, 0),
(8, 8, 120, 0),
(9, 9, 140, 0),
(10, 10, 90, 0),
(11, 11, 130, 0),
(12, 12, 170, 0),
(13, 13, 220, 0),
(14, 14, 160, 0),
(15, 15, 100, 0),
(16, 16, 180, 0),
(17, 17, 260, 0),
(18, 18, 85, 0),
(19, 19, 55, 0),
(20, 20, 75, 0),
(21, 21, 48, 0),
(22, 22, 210, 0),
(23, 23, 70, 0),
(24, 24, 145, 0);

INSERT INTO `product_review` (`id`, `product_id`, `user_id`, `order_id`, `rating`, `content`, `images`, `create_time`, `update_time`) VALUES
(1, 1, 1, 1, 5, '手机做工很扎实，系统流畅，拍照表现也稳定，日常和出差都够用。', NULL, '2026-04-22 13:10:00', '2026-04-22 13:10:00'),
(2, 3, 2, 2, 4, '性能很强，屏幕和键盘体验都不错，编译项目和写文档都很顺手。', NULL, '2026-04-21 18:20:00', '2026-04-21 18:20:00'),
(3, 8, 5, 3, 5, '降噪效果明显，通勤时提升非常大，佩戴几个小时也没有明显压头感。', NULL, '2026-04-24 20:35:00', '2026-04-24 20:35:00');

INSERT INTO `user` (`id`, `username`, `password`, `phone`, `email`, `avatar`, `status`, `role`) VALUES
(1, 'zhangsan', '$2a$10$TwICC.u3I07GLRxF9GtKQODN5ZA.pGEapkoW284iqwp3k6DTMldva', '13800138000', 'zhangsan@qq.com', 'https://api.dicebear.com/7.x/thumbs/svg?seed=zhangsan', 1, 'user'),
(2, 'lisi', '111111', '13900139000', 'lisi@163.com', 'https://api.dicebear.com/7.x/thumbs/svg?seed=lisi', 1, 'user'),
(5, 'testuser', '111111', '19182415677', 'test@example.com', 'https://api.dicebear.com/7.x/thumbs/svg?seed=testuser', 1, 'user'),
(8, 'admin', '123456', '12424453468', 'admin@example.com', 'https://api.dicebear.com/7.x/thumbs/svg?seed=admin', 1, 'admin'),
(9, 'wangwu', '123456', '13711112222', 'wangwu@example.com', 'https://api.dicebear.com/7.x/thumbs/svg?seed=wangwu', 1, 'user'),
(10, 'zhaoliu', '123456', '13611113333', 'zhaoliu@example.com', 'https://api.dicebear.com/7.x/thumbs/svg?seed=zhaoliu', 1, 'user'),
(11, 'amy', '123456', '13511114444', 'amy@example.com', 'https://api.dicebear.com/7.x/thumbs/svg?seed=amy', 1, 'user');

INSERT INTO `review_like` (`id`, `review_id`, `user_id`, `create_time`) VALUES
(1, 1, 2, '2026-04-22 14:00:00'),
(2, 1, 5, '2026-04-22 18:30:00'),
(3, 2, 1, '2026-04-21 20:15:00'),
(4, 3, 9, '2026-04-24 21:00:00');

INSERT INTO `user_address` (`id`, `user_id`, `receiver`, `phone`, `province`, `city`, `district`, `detail_address`, `is_default`) VALUES
(1, 1, 'Zhang San', '13800138000', 'Guangdong', 'Shenzhen', 'Nanshan', 'Science Park Building A-1001', 1),
(2, 1, 'Zhang San', '13800138000', 'Beijing', 'Beijing', 'Chaoyang', 'Wanda Plaza Tower A-1203', 0),
(3, 2, 'Li Si', '13900139000', 'Shanghai', 'Shanghai', 'Pudong', 'Lujiazui Financial Center 1808', 1),
(4, 5, 'Test User', '19182415677', 'Zhejiang', 'Hangzhou', 'Xihu', 'No. 88 Wenyi West Road', 1),
(5, 9, 'Wang Wu', '13711112222', 'Sichuan', 'Chengdu', 'Gaoxin', 'Tianfu Software Park 9-2201', 1),
(6, 11, 'Amy', '13511114444', 'Jiangsu', 'Nanjing', 'Jianye', 'Olympic Street 66-501', 1);

INSERT INTO `cart` (`id`, `user_id`, `product_id`, `quantity`, `is_selected`) VALUES
(1, 1, 1, 1, 1),
(2, 1, 8, 1, 1),
(3, 2, 3, 1, 1),
(4, 2, 17, 2, 0),
(5, 5, 11, 1, 1),
(6, 9, 21, 1, 1);

INSERT INTO `order` (`id`, `order_no`, `user_id`, `address_id`, `total_amount`, `pay_amount`, `status`, `pay_time`, `cancel_time`, `create_time`, `update_time`) VALUES
(1, 'ORDER202604250001', 1, 1, 8398.00, 8398.00, 1, '2026-04-22 10:20:00', NULL, '2026-04-22 10:15:00', '2026-04-22 10:20:00'),
(2, 'ORDER202604250002', 2, 3, 12999.00, 12999.00, 1, '2026-04-21 14:05:00', NULL, '2026-04-21 14:00:00', '2026-04-21 14:05:00'),
(3, 'ORDER202604250003', 5, 4, 688.00, 688.00, 2, NULL, '2026-04-24 09:45:00', '2026-04-24 09:30:00', '2026-04-24 09:45:00'),
(4, 'ORDER202604250004', 9, 5, 2298.00, 2298.00, 3, '2026-04-20 19:10:00', NULL, '2026-04-20 19:00:00', '2026-04-23 11:00:00');

INSERT INTO `order_item` (`id`, `order_id`, `product_id`, `product_name`, `product_price`, `quantity`, `total_price`, `create_time`, `price`) VALUES
(1, 1, 1, 'iPhone 15', 5999.00, 1, 5999.00, '2026-04-22 10:15:00', 5999.00),
(2, 1, 8, 'Sony WH-1000XM5', 2399.00, 1, 2399.00, '2026-04-22 10:15:00', 2399.00),
(3, 2, 3, 'MacBook Pro 14', 12999.00, 1, 12999.00, '2026-04-21 14:00:00', 12999.00),
(4, 3, 11, 'Running Shoes Air Flow', 599.00, 1, 599.00, '2026-04-24 09:30:00', 599.00),
(5, 3, 17, 'Stainless Steel Bottle', 89.00, 1, 89.00, '2026-04-24 09:30:00', 89.00),
(6, 4, 21, 'Robot Vacuum Mini', 1699.00, 1, 1699.00, '2026-04-20 19:00:00', 1699.00),
(7, 4, 24, 'Bluetooth Speaker Mini', 299.00, 2, 598.00, '2026-04-20 19:00:00', 299.00);

INSERT INTO `pay_record` (`id`, `order_no`, `user_id`, `pay_amount`, `pay_type`, `pay_status`, `pay_no`, `pay_time`) VALUES
(1, 'ORDER202604250001', 1, 8398.00, 2, 1, 'PAY202604250001', '2026-04-22 10:20:00'),
(2, 'ORDER202604250002', 2, 12999.00, 1, 1, 'PAY202604250002', '2026-04-21 14:05:00'),
(3, 'ORDER202604250004', 9, 2298.00, 1, 1, 'PAY202604250003', '2026-04-20 19:10:00');

INSERT INTO `user_points` (`id`, `user_id`, `points`, `reason`, `create_time`) VALUES
(1, 1, 84, 'Order reward', '2026-04-22 10:20:00'),
(2, 2, 129, 'Order reward', '2026-04-21 14:05:00'),
(3, 5, 20, 'Daily sign-in bonus', '2026-04-24 08:00:00'),
(4, 9, 23, 'Order reward', '2026-04-20 19:10:00'),
(5, 11, 10, 'New user bonus', '2026-04-19 12:00:00');

SET FOREIGN_KEY_CHECKS = 1;
