-- EC-01 Category module migration (MySQL 8.0+)
-- product.category_id and idx_product_category_id already exist in the current schema,
-- so this migration only creates the missing category table and preserves existing products.

USE `ec01`;

CREATE TABLE IF NOT EXISTS `category` (
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
