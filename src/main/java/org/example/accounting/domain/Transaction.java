package org.example.accounting.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 交易实体
 * 一笔业务事件（如收款、付款、转账）对应一条记录。
 * 每笔交易需要至少两条分录以满足双式记账。
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /**
     * 主键 ID，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 交易日期
     */
    @Column(name = "trade_date", nullable = false)
    private LocalDateTime tradeDate;

    /**
     * 描述/摘要
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 参考号，例如凭证号或外部单据号
     */
    @Column(name = "reference", length = 100)
    private String reference;

    /**
     * 是否已核对
     */
    @Column(name = "cleared", nullable = false)
    private Boolean cleared;

    /**
     * 创建该交易的用户名（操作员）。
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 是否被驳回（管理员驳回会置为 true）
     */
    @Column(name = "rejected", nullable = false)
    @Builder.Default
    private Boolean rejected = false;

    /**
     * 驳回理由
     */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /**
     * 驳回时间
     */
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    /**
     * 驳回人用户名
     */
    @Column(name = "rejected_by", length = 50)
    private String rejectedBy;

    /**
     * 分录列表
     */
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Split> splits;

    /**
     * 附件列表（发票等文件）。
     */
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransactionAttachment> attachments;
}



