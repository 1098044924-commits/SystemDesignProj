-- Flyway migration: create transaction_drafts table
CREATE TABLE IF NOT EXISTS transaction_drafts (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  purchase_order_id BIGINT,
  trade_date DATETIME,
  description VARCHAR(500),
  amount DECIMAL(18,2),
  created_by VARCHAR(80),
  created_at DATETIME,
  INDEX idx_purchase_order_id (purchase_order_id)
);





