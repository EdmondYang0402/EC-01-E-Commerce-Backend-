-- Remove only the EC-01 local development catalog seed.
-- Product rows are guarded by both deterministic ID range and seed marker.

SET NAMES utf8mb4;
USE `ec01`;

START TRANSACTION;

DELETE FROM `sku`
WHERE `sku_code` LIKE 'EC01-DEV-%';

DELETE FROM `product`
WHERE `id` BETWEEN 100001 AND 100100
  AND `cover_url` LIKE '%seed=ec01%';

-- Remove seeded categories only when no remaining Product or child Category uses them.
DELETE FROM `category`
WHERE `id` BETWEEN 1 AND 14
  AND NOT EXISTS (
      SELECT 1 FROM `product` WHERE `product`.`category_id` = `category`.`id`
  );

DELETE FROM `category`
WHERE `id` BETWEEN 101 AND 106
  AND NOT EXISTS (
      SELECT 1 FROM `product` WHERE `product`.`category_id` = `category`.`id`
  )
  AND NOT EXISTS (
      SELECT 1 FROM (SELECT `parent_id` FROM `category`) AS `children`
      WHERE `children`.`parent_id` = `category`.`id`
  );

COMMIT;

SELECT COUNT(*) AS remaining_seeded_products
FROM `product`
WHERE `id` BETWEEN 100001 AND 100100
  AND `cover_url` LIKE '%seed=ec01%';

SELECT COUNT(*) AS remaining_seeded_skus
FROM `sku`
WHERE `sku_code` LIKE 'EC01-DEV-%';
