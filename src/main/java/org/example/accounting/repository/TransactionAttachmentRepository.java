package org.example.accounting.repository;

import org.example.accounting.domain.TransactionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 交易附件仓储接口。
 */
public interface TransactionAttachmentRepository extends JpaRepository<TransactionAttachment, Long> {
    
    /**
     * 根据交易ID查找所有附件。
     */
    List<TransactionAttachment> findByTransactionId(Long transactionId);
}

