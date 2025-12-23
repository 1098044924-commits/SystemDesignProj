-- 导入常用会计科目账户
-- 科目代码规则：
--   ASSET (资产) -> A
--   LIABILITY (负债) -> L
--   EQUITY (权益) -> E
--   INCOME (收入) -> I
--   EXPENSE (费用) -> EX

USE gnucash_like;

-- 确保CNY货币存在（如果不存在则创建）
INSERT IGNORE INTO commodities (symbol, name, type, fraction) 
VALUES ('CNY', '人民币', 'CURRENCY', 2);

-- 获取CNY货币的ID（假设为1，如果CNY已存在则使用现有ID）
SET @cny_id = (SELECT id FROM commodities WHERE symbol = 'CNY' LIMIT 1);

-- ============================================
-- 资产类账户 (ASSET) - 代码以 A 开头
-- ============================================
INSERT IGNORE INTO accounts (name, code, type, balance, currency_id, parent_id, active) VALUES
('现金', 'A001', 'ASSET', 0.00, @cny_id, NULL, 1),
('银行存款', 'A002', 'ASSET', 0.00, @cny_id, NULL, 1),
('应收账款', 'A003', 'ASSET', 0.00, @cny_id, NULL, 1),
('预付账款', 'A004', 'ASSET', 0.00, @cny_id, NULL, 1),
('存货', 'A005', 'ASSET', 0.00, @cny_id, NULL, 1),
('固定资产', 'A006', 'ASSET', 0.00, @cny_id, NULL, 1),
('累计折旧', 'A007', 'ASSET', 0.00, @cny_id, NULL, 1),
('无形资产', 'A008', 'ASSET', 0.00, @cny_id, NULL, 1),
('其他应收款', 'A009', 'ASSET', 0.00, @cny_id, NULL, 1),
('待摊费用', 'A010', 'ASSET', 0.00, @cny_id, NULL, 1);

-- ============================================
-- 负债类账户 (LIABILITY) - 代码以 L 开头
-- ============================================
INSERT IGNORE INTO accounts (name, code, type, balance, currency_id, parent_id, active) VALUES
('应付账款', 'L001', 'LIABILITY', 0.00, @cny_id, NULL, 1),
('预收账款', 'L002', 'LIABILITY', 0.00, @cny_id, NULL, 1),
('短期借款', 'L003', 'LIABILITY', 0.00, @cny_id, NULL, 1),
('长期借款', 'L004', 'LIABILITY', 0.00, @cny_id, NULL, 1),
('应交税费', 'L005', 'LIABILITY', 0.00, @cny_id, NULL, 1),
('应付职工薪酬', 'L006', 'LIABILITY', 0.00, @cny_id, NULL, 1),
('其他应付款', 'L007', 'LIABILITY', 0.00, @cny_id, NULL, 1),
('预提费用', 'L008', 'LIABILITY', 0.00, @cny_id, NULL, 1);

-- ============================================
-- 权益类账户 (EQUITY) - 代码以 E 开头
-- ============================================
INSERT IGNORE INTO accounts (name, code, type, balance, currency_id, parent_id, active) VALUES
('实收资本', 'E001', 'EQUITY', 0.00, @cny_id, NULL, 1),
('资本公积', 'E002', 'EQUITY', 0.00, @cny_id, NULL, 1),
('盈余公积', 'E003', 'EQUITY', 0.00, @cny_id, NULL, 1),
('未分配利润', 'E004', 'EQUITY', 0.00, @cny_id, NULL, 1),
('本年利润', 'E005', 'EQUITY', 0.00, @cny_id, NULL, 1);

-- ============================================
-- 收入类账户 (INCOME) - 代码以 I 开头
-- ============================================
INSERT IGNORE INTO accounts (name, code, type, balance, currency_id, parent_id, active) VALUES
('主营业务收入', 'I001', 'INCOME', 0.00, @cny_id, NULL, 1),
('其他业务收入', 'I002', 'INCOME', 0.00, @cny_id, NULL, 1),
('投资收益', 'I003', 'INCOME', 0.00, @cny_id, NULL, 1),
('营业外收入', 'I004', 'INCOME', 0.00, @cny_id, NULL, 1),
('利息收入', 'I005', 'INCOME', 0.00, @cny_id, NULL, 1);

-- ============================================
-- 费用类账户 (EXPENSE) - 代码以 EX 开头
-- ============================================
INSERT IGNORE INTO accounts (name, code, type, balance, currency_id, parent_id, active) VALUES
('主营业务成本', 'EX001', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('销售费用', 'EX002', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('管理费用', 'EX003', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('财务费用', 'EX004', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('营业外支出', 'EX005', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('所得税费用', 'EX006', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('工资支出', 'EX007', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('水电支出', 'EX008', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('办公费用', 'EX009', 'EXPENSE', 0.00, @cny_id, NULL, 1),
('差旅费', 'EX010', 'EXPENSE', 0.00, @cny_id, NULL, 1);

-- 完成提示
SELECT '常用会计科目导入完成！' AS result;
SELECT COUNT(*) AS '导入的账户数量' FROM accounts;

