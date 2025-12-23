package org.example.accounting.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

/**
 * 交易草稿实体：由系统（例如采购审批）生成，等待管理员补充科目并正式创建交易。
 */
@Entity
@Table(name = "transaction_drafts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    @Column(name = "trade_date")
    private LocalDateTime tradeDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_by", length = 80)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}





