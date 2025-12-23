package org.example.accounting.repository;

import java.util.List;
import org.example.accounting.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByNameContainingIgnoreCaseOrProductNameContainingIgnoreCase(String name, String productName);
}






