SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `banner`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `favorite`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `appointment`;
DROP TABLE IF EXISTS `wanted`;
DROP TABLE IF EXISTS `goods`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `real_name` VARCHAR(50) NOT NULL,
  `student_no` VARCHAR(30) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `email` VARCHAR(100) NOT NULL DEFAULT '',
  `gender` VARCHAR(10) NOT NULL DEFAULT 'unknown',
  `qq` VARCHAR(20) NOT NULL DEFAULT '',
  `avatar` VARCHAR(255) NOT NULL DEFAULT '/uploads/avatar/default.png',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user',
  `status` VARCHAR(20) NOT NULL DEFAULT 'normal',
  `school` VARCHAR(100) NOT NULL DEFAULT '科成校园',
  `slogan` VARCHAR(255) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_student_no` (`student_no`),
  UNIQUE KEY `uk_sys_user_phone` (`phone`),
  KEY `idx_sys_user_role` (`role`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `sort_num` INT NOT NULL DEFAULT 0,
  `status` INT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`),
  KEY `idx_category_status` (`status`),
  KEY `idx_category_sort_num` (`sort_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `goods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `category_id` BIGINT NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `condition_level` VARCHAR(30) NOT NULL,
  `campus` VARCHAR(100) NOT NULL,
  `intro` VARCHAR(255) NOT NULL,
  `description` TEXT NOT NULL,
  `tags_json` JSON NOT NULL,
  `image_urls_json` JSON NOT NULL,
  `cover_style` VARCHAR(255) NOT NULL,
  `negotiable` TINYINT NOT NULL DEFAULT 0,
  `favorite_count` BIGINT NOT NULL DEFAULT 0,
  `status` VARCHAR(20) NOT NULL DEFAULT 'on_sale',
  `published_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_goods_user_id` (`user_id`),
  KEY `idx_goods_category_id` (`category_id`),
  KEY `idx_goods_status` (`status`),
  KEY `idx_goods_favorite_count` (`favorite_count`),
  KEY `idx_goods_published_at` (`published_at`),
  CONSTRAINT `fk_goods_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_goods_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `wanted` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `budget` VARCHAR(50) NOT NULL,
  `category_id` BIGINT NOT NULL,
  `campus` VARCHAR(100) NOT NULL,
  `deadline` DATETIME NOT NULL,
  `intro` VARCHAR(255) NOT NULL,
  `description` TEXT NOT NULL,
  `tags_json` JSON NOT NULL,
  `image_urls_json` JSON NOT NULL,
  `cover_style` VARCHAR(255) NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'buying',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_wanted_user_id` (`user_id`),
  KEY `idx_wanted_category_id` (`category_id`),
  KEY `idx_wanted_status` (`status`),
  KEY `idx_wanted_deadline` (`deadline`),
  CONSTRAINT `fk_wanted_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_wanted_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `appointment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `goods_id` BIGINT NOT NULL,
  `buyer_id` BIGINT NOT NULL,
  `seller_id` BIGINT NOT NULL,
  `intended_time` DATETIME NOT NULL,
  `intended_location` VARCHAR(200) NOT NULL,
  `remark` VARCHAR(500) NOT NULL DEFAULT '',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `handle_time` DATETIME DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_appointment_goods_id` (`goods_id`),
  KEY `idx_appointment_buyer_id` (`buyer_id`),
  KEY `idx_appointment_seller_id` (`seller_id`),
  KEY `idx_appointment_status` (`status`),
  CONSTRAINT `fk_appointment_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`),
  CONSTRAINT `fk_appointment_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_appointment_seller` FOREIGN KEY (`seller_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `goods_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` VARCHAR(200) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_comment_goods_id` (`goods_id`),
  KEY `idx_comment_user_id` (`user_id`),
  CONSTRAINT `fk_comment_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `goods_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_goods` (`user_id`, `goods_id`),
  KEY `idx_favorite_user_id` (`user_id`),
  KEY `idx_favorite_goods_id` (`goods_id`),
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_favorite_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `type` VARCHAR(50) NOT NULL DEFAULT 'system',
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `related_id` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user_id` (`user_id`),
  KEY `idx_notification_is_read` (`is_read`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `summary` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `level` VARCHAR(20) NOT NULL DEFAULT 'notice',
  `is_top` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `published_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_announcement_status` (`status`),
  KEY `idx_announcement_top` (`is_top`),
  KEY `idx_announcement_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `image_url` VARCHAR(255) NOT NULL,
  `jump_type` VARCHAR(30) NOT NULL DEFAULT 'custom',
  `jump_target` VARCHAR(255) DEFAULT NULL,
  `sort_num` INT NOT NULL DEFAULT 0,
  `status` INT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_banner_status` (`status`),
  KEY `idx_banner_sort_num` (`sort_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `sys_user`
(`id`, `username`, `password`, `real_name`, `student_no`, `phone`, `email`, `gender`, `qq`, `avatar`, `role`, `status`, `school`, `slogan`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 'admin', '123456', '校园管理员', 'ADMIN0001', '13800000000', 'admin@kecheng.edu.cn', 'unknown', '10000', '/uploads/avatar/default.png', 'admin', 'normal', '科成校园', 'Maintain trust in every campus trade.', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY), 0),
(2, 'zhangsan', '123456', '张三', '20230001', '13800000001', 'zhangsan@kecheng.edu.cn', 'male', '123456789', '/uploads/avatar/default.png', 'user', 'normal', '科成校园', 'Let every idle item be useful again.', DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY), 0),
(3, 'lisi', '123456', '李四', '20230002', '13800000002', 'lisi@kecheng.edu.cn', 'female', '987654321', '/uploads/avatar/default.png', 'user', 'normal', '科成校园', 'Organize life and make more room.', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), 0),
(4, 'wangwu', '123456', '王五', '20230003', '13800000003', 'wangwu@kecheng.edu.cn', 'male', '112233445', '/uploads/avatar/default.png', 'user', 'normal', '科成校园', 'Pass old items to the people who need them.', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), 0);

INSERT INTO `category`
(`id`, `name`, `sort_num`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, '数码产品', 1, 1, NOW(), NOW(), 0),
(2, '书籍教材', 2, 1, NOW(), NOW(), 0),
(3, '日用品', 3, 1, NOW(), NOW(), 0),
(4, '衣物鞋帽', 4, 1, NOW(), NOW(), 0),
(5, '体育用品', 5, 1, NOW(), NOW(), 0),
(6, '其他', 6, 1, NOW(), NOW(), 0);

INSERT INTO `goods`
(`id`, `user_id`, `title`, `category_id`, `price`, `original_price`, `condition_level`, `campus`, `intro`, `description`, `tags_json`, `image_urls_json`, `cover_style`, `negotiable`, `favorite_count`, `status`, `published_at`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 2, '九成新机械键盘', 1, 120.00, 299.00, '九成新', '科成主校区', '自用机械键盘，成色较好，灯光正常，功能无问题。', '自用机械键盘，成色较好，灯光正常，功能无问题，适合日常办公和游戏使用。', '["机械键盘","可议价","校内面交"]', '["/uploads/goods/keyboard_1.jpg","/uploads/goods/keyboard_2.jpg"]', 'linear-gradient(135deg, #d7ecff 0%, #8fbdf6 100%)', 0, 2, 'on_sale', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 0),
(2, 3, '高等数学教材', 2, 25.00, 58.00, '八成新', '科成图书馆区', '常用高等数学教材，内容完整，有少量笔记。', '科成大学常用高等数学教材，内容完整，有少量笔记，适合复习备考使用。', '["教材","备考","笔记完整"]', '["/uploads/goods/math_book_1.jpg"]', 'linear-gradient(135deg, #f2f7ff 0%, #b9cbff 100%)', 0, 1, 'reserved', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 0),
(3, 4, '羽毛球拍一副', 5, 60.00, 150.00, '七成新及以下', '科成体育馆', '平时打球使用，有正常磨损，拍线完好。', '平时打球使用，有正常磨损，拍线完好，可继续使用。', '["羽毛球","入门友好","已售出示例"]', '["/uploads/goods/badminton_1.jpg"]', 'linear-gradient(135deg, #eaf8ff 0%, #8fd4d9 100%)', 0, 1, 'sold', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), 0),
(4, 2, '宿舍收纳箱', 3, 18.00, 39.00, '八成新', '科成宿舍区', '透明收纳箱，容量较大，适合放衣物和书本。', '透明收纳箱，容量较大，适合放衣物和书本，宿舍搬迁低价出。', '["宿舍搬迁","收纳","低价出"]', '["/uploads/goods/box_1.jpg"]', 'linear-gradient(135deg, #eef8ff 0%, #9fd7ff 100%)', 0, 0, 'off_shelf', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0);

INSERT INTO `wanted`
(`id`, `user_id`, `title`, `budget`, `category_id`, `campus`, `deadline`, `intro`, `description`, `tags_json`, `image_urls_json`, `cover_style`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 3, '求购二手台灯', '预算 30 元左右', 3, '科成宿舍区', DATE_ADD(NOW(), INTERVAL 7 DAY), '想收一个宿舍学习用台灯，要求可以正常调光。', '想收一个宿舍学习用台灯，要求可以正常调光，外观不要太旧。', '["宿舍学习","可调光","预算友好"]', '["/uploads/wanted/lamp_1.jpg"]', 'linear-gradient(135deg, #dff8ff 0%, #80c7ff 100%)', 'buying', NOW(), NOW(), 0),
(2, 2, '求购四六级备考资料', '预算 20 元左右', 2, '科成教学区', DATE_ADD(NOW(), INTERVAL 10 DAY), '想收一套四六级备考资料，最好带真题和笔记。', '想收一套四六级备考资料，最好带真题和笔记，成色无所谓，内容完整即可。', '["四六级","真题","资料"]', '["/uploads/wanted/cet_1.jpg","/uploads/wanted/cet_2.jpg"]', 'linear-gradient(135deg, #edf7ff 0%, #9cc6ff 100%)', 'buying', NOW(), NOW(), 0),
(3, 4, '求购二手运动水杯', '预算 15 元左右', 3, '科成操场区', DATE_ADD(NOW(), INTERVAL 4 DAY), '求购一个容量较大的运动水杯。', '求购一个容量较大的运动水杯，要求无明显破损，不漏水即可。', '["运动","水杯","实用"]', '["/uploads/wanted/cup_1.jpg"]', 'linear-gradient(135deg, #f9fbff 0%, #c0d8ff 100%)', 'closed', NOW(), NOW(), 0);

INSERT INTO `comment`
(`id`, `goods_id`, `user_id`, `content`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 1, 3, '这个键盘看起来不错，请问支持小刀吗？', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(2, 1, 4, '想问一下键盘轴体是什么类型？', DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR), 0),
(3, 2, 2, '这本教材还有配套习题册吗？', DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 8 HOUR), 0);

INSERT INTO `favorite`
(`id`, `user_id`, `goods_id`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 3, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 0),
(2, 4, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
(3, 2, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
(4, 3, 3, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 0);

INSERT INTO `appointment`
(`id`, `goods_id`, `buyer_id`, `seller_id`, `intended_time`, `intended_location`, `remark`, `apply_time`, `handle_time`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 2, 2, 3, DATE_ADD(NOW(), INTERVAL 1 DAY), '科成图书馆门口', '想当面看看书本笔记情况。', DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL, 'pending', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 0),
(2, 3, 3, 4, DATE_SUB(NOW(), INTERVAL 2 DAY), '科成体育馆西门', '已经沟通过，约时间面交。', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 'completed', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0);

INSERT INTO `notification`
(`id`, `user_id`, `title`, `content`, `type`, `is_read`, `related_id`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 3, '新的预约申请', '你的商品《高等数学教材》收到一条新的预约申请，请及时处理。', 'appointment_apply', 0, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 0),
(2, 2, '预约已提交', '你已成功提交《高等数学教材》的预约申请，请等待卖家确认。', 'appointment_apply', 1, 1, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 0),
(3, 4, '交易已完成', '你的商品《羽毛球拍一副》对应预约已完成，商品状态已变更为已售出。', 'appointment_complete', 1, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(4, 3, '交易已完成', '你预约的《羽毛球拍一副》交易已完成，感谢使用校园跳蚤市场。', 'appointment_complete', 0, 2, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(5, 2, '系统公告提醒', '平台已发布新的使用说明公告，请及时查看。', 'announcement', 0, 2, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 0);

INSERT INTO `announcement`
(`id`, `title`, `summary`, `content`, `level`, `is_top`, `status`, `published_at`, `create_time`, `update_time`, `deleted`)
VALUES
(1, '欢迎使用校园跳蚤市场', '欢迎来到科成校园跳蚤市场平台。', '欢迎来到科成校园跳蚤市场平台。本平台主要面向校园内部用户，提供二手商品发布、求购信息发布以及预约面交等功能。', 'notice', 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
(2, '平台使用说明', '平台仅供校园内部学习与交流使用。', '本平台仅供校园内部学习与交流使用，交易方式为线上发布信息、线下面交。请同学们理性交易，注意核验商品信息并保护个人财产安全。', 'tip', 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
(3, '期末闲置物品集中交易提醒', '临近期末，平台将迎来闲置物品发布高峰。', '临近期末，平台将迎来闲置物品发布高峰，请大家规范填写商品信息，上传清晰图片，提高交易效率。', 'activity', 0, 1, DATE_SUB(NOW(), INTERVAL 18 HOUR), DATE_SUB(NOW(), INTERVAL 18 HOUR), DATE_SUB(NOW(), INTERVAL 18 HOUR), 0);

INSERT INTO `banner`
(`id`, `title`, `image_url`, `jump_type`, `jump_target`, `sort_num`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, '欢迎使用校园跳蚤市场', '/uploads/banner/banner1.jpg', 'announcement', '1', 1, 1, NOW(), NOW(), 0),
(2, '平台使用说明', '/uploads/banner/banner2.jpg', 'announcement', '2', 2, 1, NOW(), NOW(), 0),
(3, '热门商品推荐', '/uploads/banner/banner3.jpg', 'goods', '1', 3, 1, NOW(), NOW(), 0);

ALTER TABLE `sys_user` AUTO_INCREMENT = 5;
ALTER TABLE `category` AUTO_INCREMENT = 7;
ALTER TABLE `goods` AUTO_INCREMENT = 5;
ALTER TABLE `wanted` AUTO_INCREMENT = 4;
ALTER TABLE `appointment` AUTO_INCREMENT = 3;
ALTER TABLE `comment` AUTO_INCREMENT = 4;
ALTER TABLE `favorite` AUTO_INCREMENT = 5;
ALTER TABLE `notification` AUTO_INCREMENT = 6;
ALTER TABLE `announcement` AUTO_INCREMENT = 4;
ALTER TABLE `banner` AUTO_INCREMENT = 4;

SET FOREIGN_KEY_CHECKS = 1;
