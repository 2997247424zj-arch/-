-- 插入测试用户数据（密码统一为123456，已使用BCrypt加密）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `avatar`, `status`, `role`) VALUES
('admin', '$2a$10$fBG9ntGyE3AKPshsxdd5TetlawuRSkjNRwV0wd7JqrJQwYVQjw.tW', 'admin@example.com', '13800138000', NULL, 1, 'admin'),
('testuser', '$2a$10$fBG9ntGyE3AKPshsxdd5TetlawuRSkjNRwV0wd7JqrJQwYVQjw.tW', 'test@example.com', '13800138001', NULL, 1, 'user'),
('alice', '$2a$10$fBG9ntGyE3AKPshsxdd5TetlawuRSkjNRwV0wd7JqrJQwYVQjw.tW', 'alice@example.com', '13800138002', NULL, 1, 'user'),
('bob', '$2a$10$fBG9ntGyE3AKPshsxdd5TetlawuRSkjNRwV0wd7JqrJQwYVQjw.tW', 'bob@example.com', '13800138003', NULL, 1, 'user'),
('carol', '$2a$10$fBG9ntGyE3AKPshsxdd5TetlawuRSkjNRwV0wd7JqrJQwYVQjw.tW', 'carol@example.com', '13800138004', NULL, 1, 'user'),
('david', '$2a$10$fBG9ntGyE3AKPshsxdd5TetlawuRSkjNRwV0wd7JqrJQwYVQjw.tW', 'david@example.com', '13800138005', NULL, 1, 'user');

-- 插入商品分类数据
INSERT INTO `product_category` (`name`, `parent_id`, `sort_order`) VALUES
('电子产品', 0, 1),
('服装', 0, 2),
('图书', 0, 3),
('家居', 0, 4),
('运动户外', 0, 5),
('手机', 1, 1),
('电脑', 1, 2),
('音频设备', 1, 3),
('男装', 2, 1),
('女装', 2, 2),
('童装', 2, 3),
('文学', 3, 1),
('技术', 3, 2),
('厨具', 4, 1),
('家纺', 4, 2),
('健身器材', 5, 1),
('露营装备', 5, 2);

-- 插入商品数据
INSERT INTO `product` (`name`, `category_id`, `price`, `stock`, `description`, `img_url`) VALUES
('iPhone 15 Pro', 6, 8999.00, 100, '搭载高性能芯片与专业影像系统的旗舰手机，适合追求性能与影像体验的用户。', 'https://picsum.photos/seed/iphone15pro/800/800'),
('华为 Mate 60 Pro', 6, 6999.00, 88, '高端商务旗舰手机，支持卫星通信与超可靠续航。', 'https://picsum.photos/seed/mate60pro/800/800'),
('小米 14 Ultra', 6, 6499.00, 95, '影像能力出色的安卓旗舰，性能强劲，屏幕细腻。', 'https://picsum.photos/seed/xiaomi14ultra/800/800'),
('OPPO Find X7', 6, 5299.00, 76, '轻薄手感与影像体验兼顾，适合日常高频使用。', 'https://picsum.photos/seed/oppo-findx7/800/800'),
('MacBook Pro 14', 7, 14999.00, 45, '适合开发设计与高性能办公场景的专业笔记本。', 'https://picsum.photos/seed/macbookpro14/800/800'),
('ThinkPad X1 Carbon', 7, 9999.00, 52, '经典商务本设计，便携耐用，适合差旅办公。', 'https://picsum.photos/seed/thinkpadx1/800/800'),
('机械革命极光 Pro', 7, 7299.00, 61, '高刷新电竞屏与强悍显卡组合，兼顾游戏与创作。', 'https://picsum.photos/seed/mechrevo-pro/800/800'),
('华硕天选 Air', 7, 8499.00, 39, '轻薄机身与高性能硬件兼得，适合移动办公和娱乐。', 'https://picsum.photos/seed/asus-air/800/800'),
('索尼 WH-1000XM5', 8, 2399.00, 120, '旗舰级降噪耳机，通勤办公和长途旅行都更沉浸。', 'https://picsum.photos/seed/sony-xm5/800/800'),
('AirPods Pro 2', 8, 1799.00, 133, '主动降噪与空间音频体验优秀，苹果生态连接便捷。', 'https://picsum.photos/seed/airpodspro2/800/800'),
('漫步者 NeoBuds Pro', 8, 899.00, 148, '高性价比真无线耳机，适合通勤和运动佩戴。', 'https://picsum.photos/seed/neobudspro/800/800'),
('男士轻弹通勤夹克', 9, 329.00, 160, '简洁剪裁与耐穿面料结合，适合春秋通勤穿搭。', 'https://picsum.photos/seed/mens-jacket/800/800'),
('男士纯棉基础T恤', 9, 89.00, 280, '柔软透气，百搭易穿，是四季常备单品。', 'https://picsum.photos/seed/mens-tshirt/800/800'),
('女士法式针织开衫', 10, 259.00, 142, '柔和配色与细腻针织质感，适合叠穿和通勤。', 'https://picsum.photos/seed/women-cardigan/800/800'),
('女士高腰直筒牛仔裤', 10, 199.00, 175, '版型利落显腿长，兼顾舒适与时尚感。', 'https://picsum.photos/seed/women-jeans/800/800'),
('儿童防风连帽外套', 11, 169.00, 118, '轻盈防风，适合出游和换季穿着。', 'https://picsum.photos/seed/kids-jacket/800/800'),
('《活着》余华', 12, 39.90, 320, '经典文学作品，文字朴素却极具力量，适合深度阅读。', 'https://picsum.photos/seed/book-huozhe/800/800'),
('《百年孤独》加西亚·马尔克斯', 12, 56.00, 210, '魔幻现实主义代表作，适合喜欢文学经典的读者。', 'https://picsum.photos/seed/book-solitude/800/800'),
('《Spring 实战》', 13, 89.00, 188, '帮助开发者快速掌握 Spring 生态与工程实践。', 'https://picsum.photos/seed/book-spring/800/800'),
('《Java 编程思想》', 13, 99.00, 165, 'Java 学习经典书籍，适合作为深入理解语言特性的参考。', 'https://picsum.photos/seed/book-java/800/800'),
('《深入理解计算机系统》', 13, 128.00, 146, '系统学习计算机底层原理的高质量书籍。', 'https://picsum.photos/seed/book-csapp/800/800'),
('不粘炒锅 32cm', 14, 199.00, 132, '导热均匀，轻油烟设计，适合家庭日常烹饪。', 'https://picsum.photos/seed/wok-32/800/800'),
('多功能料理锅', 14, 369.00, 84, '煎炒蒸煮一锅多用，适合小户型与宿舍场景。', 'https://picsum.photos/seed/cooking-pot/800/800'),
('北欧风四件套', 15, 299.00, 92, '柔软亲肤，低饱和配色提升卧室氛围。', 'https://picsum.photos/seed/bedding-set/800/800'),
('记忆棉护颈枕', 15, 129.00, 147, '慢回弹支撑，帮助缓解颈部压力。', 'https://picsum.photos/seed/pillow-memory/800/800'),
('可折叠瑜伽垫', 16, 119.00, 171, '防滑耐磨，居家拉伸和瑜伽训练都适用。', 'https://picsum.photos/seed/yoga-mat/800/800'),
('可调节哑铃套装', 16, 459.00, 64, '适合居家力量训练，节省空间且重量灵活。', 'https://picsum.photos/seed/dumbbell-set/800/800'),
('智能跳绳', 16, 79.00, 230, '记录训练数据，帮助提升燃脂效率。', 'https://picsum.photos/seed/smart-rope/800/800'),
('双人露营帐篷', 17, 599.00, 56, '搭建便捷，适合周末郊游与轻露营体验。', 'https://picsum.photos/seed/camping-tent/800/800'),
('户外折叠月亮椅', 17, 159.00, 112, '轻便耐用，适合露营、钓鱼与野餐。', 'https://picsum.photos/seed/moon-chair/800/800'),
('保温露营水壶', 17, 89.00, 154, '长效保温，适合露营徒步与日常外出使用。', 'https://picsum.photos/seed/camping-kettle/800/800'),
('便携投影仪', 1, 1699.00, 70, '支持高清投屏，适合客厅观影与移动演示。', 'https://picsum.photos/seed/portable-projector/800/800'),
('智能手表 Pro', 1, 1299.00, 98, '支持心率监测、睡眠追踪与多种运动模式。', 'https://picsum.photos/seed/smart-watch-pro/800/800'),
('桌面蓝牙音箱', 8, 299.00, 136, '小巧机身也能带来饱满声场，适合桌面使用。', 'https://picsum.photos/seed/bluetooth-speaker/800/800'),
('家用空气炸锅', 14, 399.00, 89, '少油烹饪更健康，适合家庭快手料理。', 'https://picsum.photos/seed/air-fryer-home/800/800');

-- 插入测试地址数据
INSERT INTO `address` (`user_id`, `name`, `phone`, `province`, `city`, `district`, `detail`, `is_default`) VALUES
(1, '系统管理员', '13800138000', '北京市', '北京市', '朝阳区', '望京SOHO T1-1801', TRUE),
(2, '测试用户', '13800138001', '上海市', '上海市', '浦东新区', '陆家嘴金融中心A座1208', TRUE),
(2, '测试用户', '13800138001', '上海市', '上海市', '徐汇区', '漕河泾开发区创新中心6楼', FALSE),
(3, 'Alice', '13800138002', '广东省', '深圳市', '南山区', '科技园科苑路88号', TRUE),
(4, 'Bob', '13800138003', '浙江省', '杭州市', '西湖区', '文三路创业大厦B座', TRUE),
(5, 'Carol', '13800138004', '四川省', '成都市', '高新区', '天府软件园D区', TRUE),
(6, 'David', '13800138005', '湖北省', '武汉市', '洪山区', '光谷步行街3期', TRUE);

-- 插入购物车测试数据
INSERT INTO `cart` (`user_id`, `product_id`, `product_name`, `price`, `quantity`, `selected`) VALUES
(2, 9, '索尼 WH-1000XM5', 2399.00, 1, TRUE),
(2, 17, '《活着》余华', 39.90, 2, TRUE),
(3, 2, '华为 Mate 60 Pro', 6999.00, 1, TRUE),
(4, 24, '北欧风四件套', 299.00, 1, FALSE),
(5, 29, '双人露营帐篷', 599.00, 1, TRUE);

-- 插入订单数据
INSERT INTO `order` (`order_no`, `user_id`, `address_id`, `total_amount`, `status`, `create_time`, `update_time`) VALUES
('ORD202604240001', 2, 2, 10597.00, 1, '2026-04-20 10:15:00', '2026-04-20 11:10:00'),
('ORD202604240002', 2, 3, 337.90, 3, '2026-04-18 14:26:00', '2026-04-19 18:20:00'),
('ORD202604240003', 3, 4, 7358.00, 0, '2026-04-22 09:08:00', '2026-04-22 09:08:00'),
('ORD202604240004', 4, 5, 458.00, 2, '2026-04-17 21:30:00', '2026-04-17 22:10:00'),
('ORD202604240005', 5, 6, 758.00, 1, '2026-04-21 16:42:00', '2026-04-21 17:10:00'),
('ORD202604240006', 6, 7, 1748.00, 3, '2026-04-16 11:18:00', '2026-04-18 09:45:00');

-- 插入订单明细数据
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `price`, `quantity`) VALUES
(1, 1, 'iPhone 15 Pro', 8999.00, 1),
(1, 9, '索尼 WH-1000XM5', 2399.00, 1),
(2, 15, '女士高腰直筒牛仔裤', 199.00, 1),
(2, 17, '《活着》余华', 39.90, 2),
(2, 25, '记忆棉护颈枕', 129.00, 1),
(3, 2, '华为 Mate 60 Pro', 6999.00, 1),
(3, 13, '男士纯棉基础T恤', 89.00, 2),
(3, 17, '《活着》余华', 39.90, 5),
(4, 14, '女士法式针织开衫', 259.00, 1),
(4, 16, '儿童防风连帽外套', 169.00, 1),
(4, 17, '《活着》余华', 39.90, 1),
(5, 27, '可调节哑铃套装', 459.00, 1),
(5, 30, '户外折叠月亮椅', 159.00, 1),
(5, 31, '保温露营水壶', 89.00, 1),
(5, 28, '智能跳绳', 79.00, 1),
(6, 32, '便携投影仪', 1699.00, 1),
(6, 17, '《活着》余华', 39.90, 1),
(6, 34, '桌面蓝牙音箱', 299.00, 1),
(6, 35, '家用空气炸锅', 399.00, 1);

-- 插入支付记录数据
INSERT INTO `pay_record` (`order_no`, `pay_no`, `status`, `create_time`, `finish_time`) VALUES
('ORD202604240001', 'PAY202604240001', 1, '2026-04-20 10:20:00', '2026-04-20 11:05:00'),
('ORD202604240002', 'PAY202604240002', 1, '2026-04-18 14:30:00', '2026-04-18 14:35:00'),
('ORD202604240005', 'PAY202604240005', 1, '2026-04-21 16:45:00', '2026-04-21 17:05:00'),
('ORD202604240006', 'PAY202604240006', 1, '2026-04-16 11:20:00', '2026-04-16 11:26:00');
