package org.example.accounting.repository;

import java.util.List;
import org.example.accounting.domain.PurchaseOrder;
import org.example.accounting.domain.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByCreatedBy(String createdBy);
    List<PurchaseOrder> findByStatus(PurchaseStatus status);
}






