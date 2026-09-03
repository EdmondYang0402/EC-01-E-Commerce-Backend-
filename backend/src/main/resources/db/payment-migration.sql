-- EC-01 Payment V1 migration (MySQL 8.0+).
-- This script is not wired into automatic Spring Boot startup.

SET NAMES utf8mb4;
USE `ec01`;

CREATE TABLE IF NOT EXISTS `payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `payment_no` VARCHAR(64) NOT NULL,
    `provider` VARCHAR(16) NOT NULL,
    `provider_trade_no` VARCHAR(64) NULL,
    `amount` DECIMAL(12, 2) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 0,
    `paid_time` DATETIME NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_payment_no` (`payment_no`),
    UNIQUE KEY `uk_payment_provider_trade_no` (`provider_trade_no`),
    KEY `idx_payment_order_id` (`order_id`),
    KEY `idx_payment_order_status` (`order_id`, `status`),
    CONSTRAINT `chk_payment_provider` CHECK (`provider` IN ('ALIPAY')),
    CONSTRAINT `chk_payment_amount` CHECK (`amount` > 0),
    CONSTRAINT `chk_payment_status` CHECK (`status` IN (0, 1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
