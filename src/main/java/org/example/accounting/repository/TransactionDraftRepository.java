package org.example.accounting.repository;

import org.example.accounting.domain.TransactionDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionDraftRepository extends JpaRepository<TransactionDraft, Long> {
    List<TransactionDraft> findByCreatedBy(String createdBy);
}





