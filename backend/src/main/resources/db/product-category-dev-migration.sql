-- Repair category links for the deterministic EC-01 development catalog.
-- Scope is deliberately limited to product IDs 100001-100100 linked to EC01-DEV SKUs.
-- Safe to run repeatedly: every assignment is deterministic and no SKU data is touched.

SET NAMES utf8mb4;
USE `ec01`;

START TRANSACTION;

UPDATE `product`
SET `category_id` = CASE
    WHEN `id` BETWEEN 100001 AND 100002 THEN 102
    WHEN `id` IN (100003, 100008) THEN 203
    WHEN `id` IN (100004, 100005) THEN 204
    WHEN `id` IN (100006, 100025, 100026, 100027, 100028, 100029, 100030) THEN 104
    WHEN `id` = 100007 THEN 205
    WHEN `id` BETWEEN 100009 AND 100016 THEN 101
    WHEN `id` IN (100017, 100018, 100021, 100022, 100023) THEN 201
    WHEN `id` IN (100019, 100020, 100024) THEN 202
    WHEN `id` BETWEEN 100031 AND 100038 THEN 105
    WHEN `id` IN (100039, 100041, 100043, 100045) THEN 701
    WHEN `id` IN (100040, 100044) THEN 702
    WHEN `id` = 100042 THEN 703
    WHEN `id` = 100046 THEN 704
    WHEN `id` IN (100047, 100049, 100050, 100051, 100052) THEN 503
    WHEN `id` = 100048 THEN 504
    WHEN `id` = 100053 THEN 403
    WHEN `id` IN (100054, 100056, 100057, 100058) THEN 402
    WHEN `id` IN (100055, 100059) THEN 401
    WHEN `id` IN (100060, 100061, 100062, 100064, 100065, 100066, 100067) THEN 301
    WHEN `id` = 100063 THEN 302
    WHEN `id` BETWEEN 100068 AND 100074 THEN 303
    WHEN `id` BETWEEN 100075 AND 100081 THEN 305
    WHEN `id` IN (100082, 100083, 100087) THEN 501
    WHEN `id` IN (100084, 100085, 100086, 100088) THEN 502
    WHEN `id` BETWEEN 100089 AND 100093 THEN 601
    WHEN `id` = 100094 THEN 603
    WHEN `id` = 100095 THEN 802
    WHEN `id` IN (100096, 100097, 100098, 100099) THEN 803
    WHEN `id` = 100100 THEN 801
    ELSE `category_id`
END
WHERE `id` BETWEEN 100001 AND 100100
  AND EXISTS (
      SELECT 1
      FROM `sku` s
      WHERE s.`product_id` = `product`.`id`
        AND s.`sku_code` LIKE 'EC01-DEV-%'
  );

COMMIT;

SELECT COUNT(*) AS migrated_seed_product_count
FROM `product` p
JOIN `category` c ON c.`id` = p.`category_id`
WHERE p.`id` BETWEEN 100001 AND 100100
  AND EXISTS (
      SELECT 1
      FROM `sku` s
      WHERE s.`product_id` = p.`id`
        AND s.`sku_code` LIKE 'EC01-DEV-%'
  )
  AND c.`parent_id` IS NOT NULL;

SELECT c.`parent_id` AS root_category_id, COUNT(*) AS product_count
FROM `product` p
JOIN `category` c ON c.`id` = p.`category_id`
WHERE p.`id` BETWEEN 100001 AND 100100
  AND EXISTS (
      SELECT 1
      FROM `sku` s
      WHERE s.`product_id` = p.`id`
        AND s.`sku_code` LIKE 'EC01-DEV-%'
  )
GROUP BY c.`parent_id`
ORDER BY c.`parent_id`;
