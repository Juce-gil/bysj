SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `favorite`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `appointment`;
DROP TABLE IF EXISTS `wanted`;
DROP TABLE IF EXISTS `goods`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `banner`;
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
  `school` VARCHAR(100) NOT NULL DEFAULT 'Kecheng Campus',
  `slogan` VARCHAR(255) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_student_no` (`student_no`),
  UNIQUE KEY `uk_sys_user_phone` (`phone`)
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
  UNIQUE KEY `uk_category_name` (`name`)
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
  PRIMARY KEY (`id`)
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
  PRIMARY KEY (`id`)
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `goods_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` VARCHAR(200) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `goods_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_goods` (`user_id`, `goods_id`)
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
  PRIMARY KEY (`id`)
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
  PRIMARY KEY (`id`)
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `sys_user`
(`id`, `username`, `password`, `real_name`, `student_no`, `phone`, `email`, `gender`, `qq`, `avatar`, `role`, `status`, `school`, `slogan`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 'admin', '123456', 'Admin', 'ADMIN0001', '13800000000', 'admin@kecheng.edu.cn', 'unknown', '10000', '/uploads/avatar/default.png', 'admin', 'normal', 'Kecheng Campus', 'Maintain trust in every campus trade.', NOW(), NOW(), 0),
(2, 'zhangsan', '123456', 'Zhang San', '20230001', '13800000001', 'zhangsan@kecheng.edu.cn', 'male', '123456789', '/uploads/avatar/default.png', 'user', 'normal', 'Kecheng Campus', 'Let every idle item be useful again.', NOW(), NOW(), 0),
(3, 'lisi', '123456', 'Li Si', '20230002', '13800000002', 'lisi@kecheng.edu.cn', 'female', '987654321', '/uploads/avatar/default.png', 'user', 'normal', 'Kecheng Campus', 'Organize life and make more room.', NOW(), NOW(), 0);

INSERT INTO `category`
(`id`, `name`, `sort_num`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 'Digital', 1, 1, NOW(), NOW(), 0),
(2, 'Books', 2, 1, NOW(), NOW(), 0),
(3, 'Daily', 3, 1, NOW(), NOW(), 0);

INSERT INTO `announcement`
(`id`, `title`, `summary`, `content`, `level`, `is_top`, `status`, `published_at`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 'Welcome', 'Mysql integration seed announcement.', 'Seed data for mysql integration tests.', 'notice', 1, 1, NOW(), NOW(), NOW(), 0);

INSERT INTO `banner`
(`id`, `title`, `image_url`, `jump_type`, `jump_target`, `sort_num`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 'Welcome Banner', '/uploads/banner/banner1.jpg', 'announcement', '1', 1, 1, NOW(), NOW(), 0);

ALTER TABLE `sys_user` AUTO_INCREMENT = 4;
ALTER TABLE `category` AUTO_INCREMENT = 4;
ALTER TABLE `goods` AUTO_INCREMENT = 1;
ALTER TABLE `wanted` AUTO_INCREMENT = 1;
ALTER TABLE `appointment` AUTO_INCREMENT = 1;
ALTER TABLE `comment` AUTO_INCREMENT = 1;
ALTER TABLE `favorite` AUTO_INCREMENT = 1;
ALTER TABLE `notification` AUTO_INCREMENT = 1;
ALTER TABLE `announcement` AUTO_INCREMENT = 2;
ALTER TABLE `banner` AUTO_INCREMENT = 2;

SET FOREIGN_KEY_CHECKS = 1;
