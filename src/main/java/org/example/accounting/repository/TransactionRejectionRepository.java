package org.example.accounting.repository;

import org.example.accounting.domain.TransactionRejection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRejectionRepository extends JpaRepository<TransactionRejection, Long> {
    List<TransactionRejection> findByTransactionIdOrderByRejectedAtDesc(Long transactionId);
}







