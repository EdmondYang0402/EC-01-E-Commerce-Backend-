-- EC-01 Category development initialization (MySQL 8.0+)
-- Scope: rebuild category schema and category seed only.
-- This script intentionally does not update or delete any product / sku rows.

SET NAMES utf8mb4;
USE `ec01`;

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(80) NOT NULL,
    `parent_id` BIGINT NULL,
    `sort_order` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category_parent_sort` (`parent_id`, `sort_order`),
    KEY `idx_category_status` (`status`),
    CONSTRAINT `chk_category_sort_order` CHECK (`sort_order` BETWEEN 0 AND 9999),
    CONSTRAINT `chk_category_status` CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add product.category_id only when the current database does not have it.
SET @has_product_category_id = (
    SELECT COUNT(*)
    FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE()
      AND `table_name` = 'product'
      AND `column_name` = 'category_id'
);
SET @add_product_category_id_sql = IF(
    @has_product_category_id = 0,
    'ALTER TABLE `product` ADD COLUMN `category_id` BIGINT NULL',
    'SELECT 1'
);
PREPARE add_product_category_id_stmt FROM @add_product_category_id_sql;
EXECUTE add_product_category_id_stmt;
DEALLOCATE PREPARE add_product_category_id_stmt;

-- Add an index only when category_id is not already the leading column of an index.
SET @has_product_category_index = (
    SELECT COUNT(*)
    FROM `information_schema`.`statistics`
    WHERE `table_schema` = DATABASE()
      AND `table_name` = 'product'
      AND `column_name` = 'category_id'
      AND `seq_in_index` = 1
);
SET @add_product_category_index_sql = IF(
    @has_product_category_index = 0,
    'ALTER TABLE `product` ADD INDEX `idx_product_category_id` (`category_id`)',
    'SELECT 1'
);
PREPARE add_product_category_index_stmt FROM @add_product_category_index_sql;
EXECUTE add_product_category_index_stmt;
DEALLOCATE PREPARE add_product_category_index_stmt;

-- Eight root categories. parent_id = NULL identifies the first level.
INSERT INTO `category` (`id`, `name`, `parent_id`, `sort_order`, `status`) VALUES
(1, '电子数码', NULL, 10, 1),
(2, '电脑周边', NULL, 20, 1),
(3, '服装鞋包', NULL, 30, 1),
(4, '办公用品', NULL, 40, 1),
(5, '家居生活', NULL, 50, 1),
(6, '运动户外', NULL, 60, 1),
(7, '家用电器', NULL, 70, 1),
(8, '游戏娱乐', NULL, 80, 1);

-- Thirty-five second-level categories. One disabled row is kept for status testing.
INSERT INTO `category` (`id`, `name`, `parent_id`, `sort_order`, `status`) VALUES
(101, '手机配件', 1, 10, 1),
(102, '笔记本与主机', 1, 20, 1),
(103, '平板与智能设备', 1, 30, 1),
(104, '显示器', 1, 40, 1),
(105, '耳机与音频', 1, 50, 1),

(201, '键盘', 2, 10, 1),
(202, '鼠标', 2, 20, 1),
(203, '存储设备', 2, 30, 1),
(204, '充电与扩展', 2, 40, 1),
(205, '网络设备', 2, 50, 1),

(301, '上衣', 3, 10, 1),
(302, '裤装', 3, 20, 1),
(303, '鞋类', 3, 30, 1),
(304, '服饰配件', 3, 40, 1),
(305, '背包与箱包', 3, 50, 1),

(401, '文具纸品', 4, 10, 1),
(402, '桌面收纳', 4, 20, 1),
(403, '办公设备', 4, 30, 1),
(404, '打印耗材', 4, 40, 1),

(501, '家具', 5, 10, 1),
(502, '家居装饰', 5, 20, 1),
(503, '生活日用', 5, 30, 1),
(504, '清洁与收纳', 5, 40, 1),

(601, '健身训练', 6, 10, 1),
(602, '户外装备', 6, 20, 1),
(603, '球类运动', 6, 30, 1),
(604, '骑行用品', 6, 40, 1),

(701, '厨房电器', 7, 10, 1),
(702, '环境电器', 7, 20, 1),
(703, '清洁电器', 7, 30, 1),
(704, '个人护理', 7, 40, 1),

(801, '游戏主机配件', 8, 10, 1),
(802, '游戏手柄', 8, 20, 1),
(803, '电竞外设', 8, 30, 1),
(804, '桌游益智', 8, 40, 0);

-- Verification queries.
SELECT COUNT(*) AS root_category_count
FROM `category`
WHERE `parent_id` IS NULL;

SELECT COUNT(*) AS child_category_count
FROM `category`
WHERE `parent_id` IS NOT NULL;

SELECT COUNT(*) AS disabled_child_category_count
FROM `category`
WHERE `parent_id` IS NOT NULL AND `status` = 0;

SELECT COUNT(*) AS product_category_column_count
FROM `information_schema`.`columns`
WHERE `table_schema` = DATABASE()
  AND `table_name` = 'product'
  AND `column_name` = 'category_id';

SELECT COUNT(*) AS product_category_index_count
FROM `information_schema`.`statistics`
WHERE `table_schema` = DATABASE()
  AND `table_name` = 'product'
  AND `column_name` = 'category_id'
  AND `seq_in_index` = 1;
