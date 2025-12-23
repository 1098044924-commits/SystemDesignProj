-- 商品/服务/货币表
CREATE TABLE commodities (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    symbol VARCHAR(50) NOT NULL COMMENT '符号/代码，例如：CNY, USD, AAPL',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    type VARCHAR(30) NOT NULL COMMENT '类型：CURRENCY/STOCK/FUND/GOOD_OR_SERVICE',
    fraction INT NOT NULL COMMENT '小数位数',
    PRIMARY KEY (id),
    UNIQUE KEY uk_commodities_symbol (symbol)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '商品/服务/货币表';

-- 账户表
CREATE TABLE accounts (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '账户名称',
    code VARCHAR(50) NOT NULL COMMENT '科目代码，例如：1010.01',
    type VARCHAR(20) NOT NULL COMMENT '账户类型：ASSET/LIABILITY/EQUITY/INCOME/EXPENSE',
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT '当前余额',
    currency_id BIGINT NOT NULL COMMENT '货币ID，关联 commodities 表',
    parent_id BIGINT NULL COMMENT '父账户ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_accounts_code (code),
    KEY idx_accounts_parent (parent_id),
    KEY idx_accounts_currency (currency_id),
    CONSTRAINT fk_accounts_currency
        FOREIGN KEY (currency_id) REFERENCES commodities (id),
    CONSTRAINT fk_accounts_parent
        FOREIGN KEY (parent_id) REFERENCES accounts (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '账户/科目表';

-- 交易表
CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    trade_date DATETIME NOT NULL COMMENT '交易日期',
    description VARCHAR(255) NULL COMMENT '描述/摘要',
    reference VARCHAR(100) NULL COMMENT '参考号',
    cleared TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已核对',
    PRIMARY KEY (id),
    KEY idx_transactions_trade_date (trade_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '交易表';

-- 分录表
CREATE TABLE splits (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    transaction_id BIGINT NOT NULL COMMENT '交易ID',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    amount DECIMAL(18, 2) NOT NULL COMMENT '金额',
    direction VARCHAR(10) NOT NULL COMMENT '借贷方向：DEBIT/CREDIT',
    quantity DECIMAL(18, 6) NULL COMMENT '数量（用于股票等）',
    price DECIMAL(18, 6) NULL COMMENT '单价（用于股票等）',
    commodity_id BIGINT NULL COMMENT '商品/证券ID',
    memo VARCHAR(255) NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_splits_transaction (transaction_id),
    KEY idx_splits_account (account_id),
    KEY idx_splits_commodity (commodity_id),
    CONSTRAINT fk_splits_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT fk_splits_account
        FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_splits_commodity
        FOREIGN KEY (commodity_id) REFERENCES commodities (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '分录表';












