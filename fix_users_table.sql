-- 修复 users 表结构：将 email 字段改为 real_name
-- 使用方法：在 MySQL 客户端中执行此脚本

USE gnucash_like;

-- 检查 email 字段是否存在，如果存在则重命名为 real_name
-- 如果 real_name 字段已存在，则先删除 email 字段
SET @email_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'gnucash_like' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'email'
);

SET @real_name_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'gnucash_like' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'real_name'
);

-- 如果 email 字段存在且 real_name 不存在，则重命名
-- 如果 email 字段存在且 real_name 也存在，则删除 email
-- 如果 email 不存在但 real_name 也不存在，则添加 real_name

SET @sql = '';
IF @email_exists > 0 AND @real_name_exists = 0 THEN
    SET @sql = 'ALTER TABLE users CHANGE COLUMN email real_name VARCHAR(100) NOT NULL COMMENT ''真实姓名'';';
ELSEIF @email_exists > 0 AND @real_name_exists > 0 THEN
    SET @sql = 'ALTER TABLE users DROP COLUMN email;';
ELSEIF @email_exists = 0 AND @real_name_exists = 0 THEN
    SET @sql = 'ALTER TABLE users ADD COLUMN real_name VARCHAR(100) NOT NULL COMMENT ''真实姓名'';';
END IF;

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT 'users 表结构已更新！' AS result;

