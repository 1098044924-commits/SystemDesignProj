-- Add rejection fields to transactions table
ALTER TABLE transactions
  ADD COLUMN rejected TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否被驳回',
  ADD COLUMN rejection_reason VARCHAR(500) NULL COMMENT '驳回理由',
  ADD COLUMN rejected_at DATETIME NULL COMMENT '驳回时间',
  ADD COLUMN rejected_by VARCHAR(50) NULL COMMENT '被谁驳回（用户名）';







