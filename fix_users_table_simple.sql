-- 修复 users 表结构：将 email 字段改为 real_name（简化版本）
-- 使用方法：在 MySQL 客户端中执行此脚本

USE gnucash_like;

-- 方法1：如果 email 字段存在，直接重命名为 real_name
ALTER TABLE users 
    CHANGE COLUMN email real_name VARCHAR(100) NOT NULL COMMENT '真实姓名';

-- 如果上面的语句报错说 real_name 已存在，则执行下面的语句删除 email 字段
-- ALTER TABLE users DROP COLUMN email;

SELECT 'users 表结构已更新！' AS result;

