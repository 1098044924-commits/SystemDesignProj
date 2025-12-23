-- 清空所有数据的 SQL 脚本
-- 使用方法：在 MySQL 客户端中执行此脚本
-- 注意：此操作不可逆，请谨慎使用！

USE gnucash_like;

-- 1. 删除分录表（splits）- 依赖 transactions 和 accounts
DELETE FROM splits;

-- 2. 删除交易表（transactions）
DELETE FROM transactions;

-- 3. 先清空账户表的父账户引用（避免外键约束问题）
UPDATE accounts SET parent_id = NULL WHERE parent_id IS NOT NULL;

-- 4. 删除账户表（accounts）- 依赖 commodities
DELETE FROM accounts;

-- 5. 删除商品/货币表（commodities）
DELETE FROM commodities;

-- 6. 删除用户表（users）- 可选，如果需要保留用户数据可以注释掉
-- DELETE FROM users;

-- 重置自增ID（可选，如果需要从1开始重新计数）
-- ALTER TABLE splits AUTO_INCREMENT = 1;
-- ALTER TABLE transactions AUTO_INCREMENT = 1;
-- ALTER TABLE accounts AUTO_INCREMENT = 1;
-- ALTER TABLE commodities AUTO_INCREMENT = 1;
-- ALTER TABLE users AUTO_INCREMENT = 1;

-- 完成！
SELECT '所有数据已清空！' AS result;

