package org.example.accounting.repository;

import java.util.List;
import org.example.accounting.domain.PurchaseOrderAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderAuditRepository extends JpaRepository<PurchaseOrderAudit, Long> {
    List<PurchaseOrderAudit> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}






