-- 清空所有数据的脚本
-- 注意：由于外键约束，需要按特定顺序删除数据

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

