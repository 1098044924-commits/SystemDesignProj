package org.example.accounting.repository;

import org.example.accounting.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByCleared(Boolean cleared, Pageable pageable);

    long countByCreatedByAndRejectedTrueAndClearedFalse(String createdBy);

    Page<Transaction> findByCreatedByAndRejectedTrueAndClearedFalse(String createdBy, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("select t from Transaction t where t.cleared = true and (:q is null or lower(t.description) like concat('%', lower(:q), '%') or lower(t.reference) like concat('%', lower(:q), '%'))")
    Page<Transaction> searchCleared(@org.springframework.data.repository.query.Param("q") String q, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("select t from Transaction t where t.cleared = true and t.createdBy = :createdBy and (:q is null or lower(t.description) like concat('%', lower(:q), '%') or lower(t.reference) like concat('%', lower(:q), '%'))")
    Page<Transaction> searchClearedByUser(@org.springframework.data.repository.query.Param("createdBy") String createdBy, @org.springframework.data.repository.query.Param("q") String q, Pageable pageable);
}


