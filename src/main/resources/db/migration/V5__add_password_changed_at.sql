-- 添加密码最后更新时间字段
ALTER TABLE users 
    ADD COLUMN password_changed_at DATETIME NULL COMMENT '密码最后更新时间';

