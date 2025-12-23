package org.example.accounting.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * 采购订单审核历史记录
 */
@Entity
@Table(name = "purchase_order_audits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(length = 80)
    private String action;

    @Column(length = 80)
    private String actor;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}






