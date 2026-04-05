-- =========================================================
-- 项目名称：基于 Spring Boot 和 Vue 的校园二手交易平台设计与实现
-- 网站名称：校园跳蚤市场
-- 学校名称：科成
-- 数据库：kecheng_campus_market
-- MySQL版本：8.x
-- 字符集：utf8mb4
-- 说明：
-- 1. 本脚本包含建库、建表、索引、初始化数据
-- 2. 用户初始化密码暂时使用明文 123456
-- 3. 若后端使用 BCrypt，请后续自行替换密码字段
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- 1. 创建数据库
-- =========================================================
DROP DATABASE IF EXISTS `kecheng_campus_market`;
CREATE DATABASE `kecheng_campus_market`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE `kecheng_campus_market`;

-- =========================================================
-- 2. 删除旧表（按依赖顺序倒序删除）
-- =========================================================
DROP TABLE IF EXISTS `banner`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `favorite`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `appointment`;
DROP TABLE IF EXISTS `wanted_image`;
DROP TABLE IF EXISTS `wanted`;
DROP TABLE IF EXISTS `goods_image`;
DROP TABLE IF EXISTS `goods`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `sys_user`;

-- =========================================================
-- 3. 用户表
-- =========================================================
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（建议后续替换为BCrypt密文）',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `student_no` VARCHAR(30) NOT NULL COMMENT '学号',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `gender` VARCHAR(10) DEFAULT 'unknown' COMMENT '性别：male/female/unknown',
  `qq` VARCHAR(20) DEFAULT NULL COMMENT 'QQ号',
  `avatar` VARCHAR(255) DEFAULT '/upload/avatar/default.png' COMMENT '头像地址',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：user/admin',
  `status` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '状态：normal/disabled',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  UNIQUE KEY `uk_sys_user_student_no` (`student_no`),
  UNIQUE KEY `uk_sys_user_phone` (`phone`),
  KEY `idx_sys_user_role` (`role`),
  KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =========================================================
-- 4. 分类表
-- =========================================================
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `sort_num` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`name`),
  KEY `idx_category_status` (`status`),
  KEY `idx_category_sort_num` (`sort_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- =========================================================
-- 5. 商品表
-- =========================================================
CREATE TABLE `goods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '发布人ID',
  `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '现价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `condition_level` VARCHAR(30) NOT NULL COMMENT '成色：new/ninety/eighty/below_seventy',
  `description` TEXT NOT NULL COMMENT '商品描述',
  `negotiable` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否可议价：0否 1是',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `status` VARCHAR(20) NOT NULL DEFAULT 'on_sale' COMMENT '商品状态：on_sale/reserved/sold/off_shelf',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_goods_user_id` (`user_id`),
  KEY `idx_goods_category_id` (`category_id`),
  KEY `idx_goods_status` (`status`),
  KEY `idx_goods_favorite_count` (`favorite_count`),
  KEY `idx_goods_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- =========================================================
-- 6. 商品图片表
-- =========================================================
CREATE TABLE `goods_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
  `sort_num` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_goods_image_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- =========================================================
-- 7. 求购表
-- =========================================================
CREATE TABLE `wanted` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '发布人ID',
  `title` VARCHAR(100) NOT NULL COMMENT '求购标题',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `expected_price` DECIMAL(10,2) NOT NULL COMMENT '期望价格',
  `description` TEXT NOT NULL COMMENT '求购描述',
  `status` VARCHAR(20) NOT NULL DEFAULT 'buying' COMMENT '状态：buying/finished/closed',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_wanted_user_id` (`user_id`),
  KEY `idx_wanted_category_id` (`category_id`),
  KEY `idx_wanted_status` (`status`),
  KEY `idx_wanted_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求购表';

-- =========================================================
-- 8. 求购图片表
-- =========================================================
CREATE TABLE `wanted_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `wanted_id` BIGINT NOT NULL COMMENT '求购ID',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
  `sort_num` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_wanted_image_wanted_id` (`wanted_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求购图片表';

-- =========================================================
-- 9. 预约表
-- =========================================================
CREATE TABLE `appointment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `intended_time` DATETIME NOT NULL COMMENT '意向交易时间',
  `intended_location` VARCHAR(200) NOT NULL COMMENT '意向交易地点',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending/agreed/rejected/cancelled/completed',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_appointment_goods_id` (`goods_id`),
  KEY `idx_appointment_buyer_id` (`buyer_id`),
  KEY `idx_appointment_seller_id` (`seller_id`),
  KEY `idx_appointment_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- =========================================================
-- 10. 评论表
-- 注意：comment 是SQL关键字，这里使用反引号包裹
-- =========================================================
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `content` VARCHAR(200) NOT NULL COMMENT '评论内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_comment_goods_id` (`goods_id`),
  KEY `idx_comment_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评论表';

-- =========================================================
-- 11. 收藏表
-- =========================================================
CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `goods_id` BIGINT NOT NULL COMMENT '商品ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_user_goods` (`user_id`, `goods_id`),
  KEY `idx_favorite_user_id` (`user_id`),
  KEY `idx_favorite_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- =========================================================
-- 12. 通知表
-- =========================================================
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '接收人ID',
  `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `type` VARCHAR(50) NOT NULL DEFAULT 'system' COMMENT '通知类型',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_notification_user_id` (`user_id`),
  KEY `idx_notification_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- =========================================================
-- 13. 公告表
-- =========================================================
CREATE TABLE `announcement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶：0否 1是',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '发布状态：0未发布 1已发布',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_announcement_is_top` (`is_top`),
  KEY `idx_announcement_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- =========================================================
-- 14. 轮播图表
-- =========================================================
CREATE TABLE `banner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '轮播图标题',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
  `jump_type` VARCHAR(30) NOT NULL DEFAULT 'custom' COMMENT '跳转类型：goods/announcement/custom',
  `jump_target` VARCHAR(255) DEFAULT NULL COMMENT '跳转目标',
  `sort_num` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_banner_status` (`status`),
  KEY `idx_banner_sort_num` (`sort_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- =========================================================
-- 15. 初始化用户数据
-- 注意：
-- 这里 password 临时写成 123456
-- 若后端采用 BCrypt，请自行替换成加密后的密文
-- =========================================================
INSERT INTO `sys_user`
(`id`, `username`, `password`, `real_name`, `student_no`, `phone`, `email`, `gender`, `qq`, `avatar`, `role`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 'admin', '123456', '管理员', 'ADMIN0001', '13800000000', 'admin@kecheng.edu.cn', 'unknown', '10000', '/upload/avatar/default.png', 'admin', 'normal', NOW(), NOW(), 0),
(2, 'zhangsan', '123456', '张三', '20230001', '13800000001', 'zhangsan@kecheng.edu.cn', 'male', '123456789', '/upload/avatar/default.png', 'user', 'normal', NOW(), NOW(), 0),
(3, 'lisi', '123456', '李四', '20230002', '13800000002', 'lisi@kecheng.edu.cn', 'female', '987654321', '/upload/avatar/default.png', 'user', 'normal', NOW(), NOW(), 0),
(4, 'wangwu', '123456', '王五', '20230003', '13800000003', 'wangwu@kecheng.edu.cn', 'male', '112233445', '/upload/avatar/default.png', 'user', 'normal', NOW(), NOW(), 0);

-- =========================================================
-- 16. 初始化分类数据
-- =========================================================
INSERT INTO `category`
(`id`, `name`, `sort_num`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, '数码产品', 1, 1, NOW(), NOW(), 0),
(2, '书籍教材', 2, 1, NOW(), NOW(), 0),
(3, '日用品', 3, 1, NOW(), NOW(), 0),
(4, '衣物鞋帽', 4, 1, NOW(), NOW(), 0),
(5, '体育用品', 5, 1, NOW(), NOW(), 0),
(6, '其他', 6, 1, NOW(), NOW(), 0);

-- =========================================================
-- 17. 初始化商品数据
-- =========================================================
INSERT INTO `goods`
(`id`, `user_id`, `title`, `category_id`, `price`, `original_price`, `condition_level`, `description`, `negotiable`, `favorite_count`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 2, '九成新机械键盘', 1, 120.00, 299.00, 'ninety', '自用机械键盘，成色较好，灯光正常，功能无问题，适合日常办公和游戏使用。', 1, 2, 'on_sale', NOW(), NOW(), 0),
(2, 3, '高等数学教材', 2, 25.00, 58.00, 'eighty', '科成大学常用高等数学教材，内容完整，有少量笔记，适合复习备考使用。', 0, 1, 'reserved', NOW(), NOW(), 0),
(3, 4, '羽毛球拍一副', 5, 60.00, 150.00, 'below_seventy', '平时打球使用，有正常磨损，拍线完好，可继续使用。', 1, 1, 'sold', NOW(), NOW(), 0),
(4, 2, '宿舍收纳箱', 3, 18.00, 39.00, 'eighty', '透明收纳箱，容量较大，适合放衣物和书本，宿舍搬迁低价出。', 1, 0, 'off_shelf', NOW(), NOW(), 0);

-- =========================================================
-- 18. 初始化商品图片数据
-- =========================================================
INSERT INTO `goods_image`
(`id`, `goods_id`, `image_url`, `sort_num`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 1, '/upload/goods/keyboard_1.jpg', 1, NOW(), NOW(), 0),
(2, 1, '/upload/goods/keyboard_2.jpg', 2, NOW(), NOW(), 0),
(3, 2, '/upload/goods/math_book_1.jpg', 1, NOW(), NOW(), 0),
(4, 3, '/upload/goods/badminton_1.jpg', 1, NOW(), NOW(), 0),
(5, 4, '/upload/goods/box_1.jpg', 1, NOW(), NOW(), 0);

-- =========================================================
-- 19. 初始化求购数据
-- =========================================================
INSERT INTO `wanted`
(`id`, `user_id`, `title`, `category_id`, `expected_price`, `description`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 3, '求购二手台灯', 3, 30.00, '想收一个宿舍学习用台灯，要求可以正常调光，外观不要太旧。', 'buying', NOW(), NOW(), 0),
(2, 2, '求购四六级备考资料', 2, 20.00, '想收一套四六级备考资料，最好带真题和笔记，成色无所谓，内容完整即可。', 'buying', NOW(), NOW(), 0),
(3, 4, '求购二手运动水杯', 3, 15.00, '求购一个容量较大的运动水杯，要求无明显破损，不漏水即可。', 'closed', NOW(), NOW(), 0);

-- =========================================================
-- 20. 初始化求购图片数据
-- =========================================================
INSERT INTO `wanted_image`
(`id`, `wanted_id`, `image_url`, `sort_num`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 1, '/upload/wanted/lamp_1.jpg', 1, NOW(), NOW(), 0),
(2, 2, '/upload/wanted/cet_1.jpg', 1, NOW(), NOW(), 0),
(3, 2, '/upload/wanted/cet_2.jpg', 2, NOW(), NOW(), 0),
(4, 3, '/upload/wanted/cup_1.jpg', 1, NOW(), NOW(), 0);

-- =========================================================
-- 21. 初始化评论数据
-- =========================================================
INSERT INTO `comment`
(`id`, `goods_id`, `user_id`, `content`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 1, 3, '这个键盘看起来不错，请问支持小刀吗？', NOW(), NOW(), 0),
(2, 1, 4, '想问一下键盘轴体是什么类型？', NOW(), NOW(), 0),
(3, 2, 2, '这本教材还有配套习题册吗？', NOW(), NOW(), 0),
(4, 3, 3, '羽毛球拍已经卖出去了吗？', NOW(), NOW(), 0);

-- =========================================================
-- 22. 初始化收藏数据
-- 说明：
-- 这里的数据与 goods 表中的 favorite_count 对应
-- goods 1 => 2次收藏
-- goods 2 => 1次收藏
-- goods 3 => 1次收藏
-- goods 4 => 0次收藏
-- =========================================================
INSERT INTO `favorite`
(`id`, `user_id`, `goods_id`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 3, 1, NOW(), NOW(), 0),
(2, 4, 1, NOW(), NOW(), 0),
(3, 2, 2, NOW(), NOW(), 0),
(4, 3, 3, NOW(), NOW(), 0);

-- =========================================================
-- 23. 初始化预约数据
-- 说明：
-- goods 2 当前状态 reserved，对应一条待确认预约
-- goods 3 当前状态 sold，对应一条已完成预约
-- =========================================================
INSERT INTO `appointment`
(`id`, `goods_id`, `buyer_id`, `seller_id`, `intended_time`, `intended_location`, `remark`, `apply_time`, `handle_time`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 2, 2, 3, DATE_ADD(NOW(), INTERVAL 1 DAY), '科成图书馆门口', '想当面看看书本笔记情况，如果合适就直接交易。', NOW(), NULL, 'pending', NOW(), NOW(), 0),
(2, 3, 3, 4, DATE_SUB(NOW(), INTERVAL 2 DAY), '科成体育馆西门', '已经沟通过，约时间面交。', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 'completed', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 0);

-- =========================================================
-- 24. 初始化通知数据
-- =========================================================
INSERT INTO `notification`
(`id`, `user_id`, `title`, `content`, `type`, `is_read`, `related_id`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 3, '新的预约申请', '你的商品《高等数学教材》收到一条新的预约申请，请及时处理。', 'appointment_apply', 0, 1, NOW(), NOW(), 0),
(2, 2, '预约已提交', '你已成功提交《高等数学教材》的预约申请，请等待卖家确认。', 'appointment_apply', 1, 1, NOW(), NOW(), 0),
(3, 4, '交易已完成', '你的商品《羽毛球拍一副》对应预约已完成，商品状态已变更为已售出。', 'appointment_complete', 1, 2, NOW(), NOW(), 0),
(4, 3, '交易已完成', '你预约的《羽毛球拍一副》交易已完成，感谢使用校园跳蚤市场。', 'appointment_complete', 0, 2, NOW(), NOW(), 0),
(5, 2, '系统公告提醒', '平台已发布新的使用说明公告，请及时查看。', 'announcement', 0, 2, NOW(), NOW(), 0);

-- =========================================================
-- 25. 初始化公告数据
-- =========================================================
INSERT INTO `announcement`
(`id`, `title`, `content`, `is_top`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, '欢迎使用校园跳蚤市场', '欢迎来到科成校园跳蚤市场平台。本平台主要面向校园内部用户，提供二手商品发布、求购信息发布以及预约面交等功能。', 1, 1, NOW(), NOW(), 0),
(2, '平台使用说明', '本平台仅供校园内部学习与交流使用，交易方式为线上发布信息、线下面交。请同学们理性交易，注意核验商品信息并保护个人财产安全。', 0, 1, NOW(), NOW(), 0),
(3, '期末闲置物品集中交易提醒', '临近期末，平台将迎来闲置物品发布高峰，请大家规范填写商品信息，上传清晰图片，提高交易效率。', 0, 1, NOW(), NOW(), 0);

-- =========================================================
-- 26. 初始化轮播图数据
-- =========================================================
INSERT INTO `banner`
(`id`, `title`, `image_url`, `jump_type`, `jump_target`, `sort_num`, `status`, `create_time`, `update_time`, `deleted`)
VALUES
(1, '欢迎使用校园跳蚤市场', '/upload/banner/banner1.jpg', 'announcement', '1', 1, 1, NOW(), NOW(), 0),
(2, '平台使用说明', '/upload/banner/banner2.jpg', 'announcement', '2', 2, 1, NOW(), NOW(), 0),
(3, '热门商品推荐', '/upload/banner/banner3.jpg', 'goods', '1', 3, 1, NOW(), NOW(), 0);

-- =========================================================
-- 27. 可选：重置自增起始值（按当前初始化数据顺延）
-- =========================================================
ALTER TABLE `sys_user` AUTO_INCREMENT = 5;
ALTER TABLE `category` AUTO_INCREMENT = 7;
ALTER TABLE `goods` AUTO_INCREMENT = 5;
ALTER TABLE `goods_image` AUTO_INCREMENT = 6;
ALTER TABLE `wanted` AUTO_INCREMENT = 4;
ALTER TABLE `wanted_image` AUTO_INCREMENT = 5;
ALTER TABLE `appointment` AUTO_INCREMENT = 3;
ALTER TABLE `comment` AUTO_INCREMENT = 5;
ALTER TABLE `favorite` AUTO_INCREMENT = 5;
ALTER TABLE `notification` AUTO_INCREMENT = 6;
ALTER TABLE `announcement` AUTO_INCREMENT = 4;
ALTER TABLE `banner` AUTO_INCREMENT = 4;

-- =========================================================
-- 28. 恢复外键检查
-- =========================================================
SET FOREIGN_KEY_CHECKS = 1;

