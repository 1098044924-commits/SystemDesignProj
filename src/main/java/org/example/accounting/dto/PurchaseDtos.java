package org.example.accounting.dto;

import java.math.BigDecimal;
import lombok.Data;

public class PurchaseDtos {

    @Data
    public static class CreateSupplierRequest {
        private String name;
        private String location;
        private String email;
        private String productName;
        private BigDecimal unitPrice;
    }

    @Data
    public static class CreatePurchaseRequest {
        private String productName;
        private Long supplierId;
        private Integer quantity;
    }
    
    @Data
    public static class ReviewResult {
        private org.example.accounting.domain.PurchaseOrder order;
        private Long draftTransactionId;
    }
}


