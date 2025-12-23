-- 添加交易创建者字段
ALTER TABLE transactions 
    ADD COLUMN created_by VARCHAR(50) NULL COMMENT '创建该交易的用户名（操作员）';

