-- Apply once to a database created before the EC-01 Admin role model.
ALTER TABLE `users`
    ADD COLUMN `role` VARCHAR(16) NOT NULL DEFAULT 'USER' AFTER `status`,
    ADD KEY `idx_user_role_status` (`role`, `status`),
    ADD CONSTRAINT `chk_user_role` CHECK (`role` IN ('USER', 'ADMIN'));

-- Promote one reviewed account explicitly:
-- UPDATE `users` SET `role` = 'ADMIN' WHERE `username` = 'your-admin-username';
