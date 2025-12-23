-- 创建交易附件表
CREATE TABLE transaction_attachments (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    transaction_id BIGINT NOT NULL COMMENT '交易ID',
    original_filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_filename VARCHAR(255) NOT NULL COMMENT '存储的文件名（UUID）',
    content_type VARCHAR(100) NULL COMMENT '文件类型（MIME类型）',
    file_size BIGINT NULL COMMENT '文件大小（字节）',
    uploaded_at DATETIME NOT NULL COMMENT '上传时间',
    PRIMARY KEY (id),
    KEY idx_transaction_id (transaction_id),
    CONSTRAINT fk_attachments_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '交易附件表（发票等文件）';

