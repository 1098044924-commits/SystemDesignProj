-- 将users表的email字段改为real_name字段
ALTER TABLE users 
    CHANGE COLUMN email real_name VARCHAR(100) NOT NULL COMMENT '真实姓名';

