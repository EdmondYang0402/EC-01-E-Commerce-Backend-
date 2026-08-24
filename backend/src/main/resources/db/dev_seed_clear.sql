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

-- Category is now an independent module seed and is intentionally preserved.

COMMIT;

SELECT COUNT(*) AS remaining_seeded_products
FROM `product`
WHERE `id` BETWEEN 100001 AND 100100
  AND `cover_url` LIKE '%seed=ec01%';

SELECT COUNT(*) AS remaining_seeded_skus
FROM `sku`
WHERE `sku_code` LIKE 'EC01-DEV-%';
