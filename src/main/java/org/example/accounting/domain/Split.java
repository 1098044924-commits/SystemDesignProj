package org.example.accounting.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 分录实体
 * 每条分录关联一个账户和一笔交易，并标记借贷方向与金额。
 */
@Entity
@Table(name = "splits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Split {

    /**
     * 主键 ID，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属交易
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    /**
     * 关联账户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * 金额，使用 BigDecimal 保证精度
     */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /**
     * 借贷方向：借方或贷方
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 10)
    private DebitCredit direction;

    /**
     * 数量（例如股票数量），普通货币交易可为空
     */
    @Column(name = "quantity", precision = 18, scale = 6)
    private BigDecimal quantity;

    /**
     * 单价，用于股票/基金/商品交易
     */
    @Column(name = "price", precision = 18, scale = 6)
    private BigDecimal price;

    /**
     * 对应商品/证券，可为空
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commodity_id")
    private Commodity commodity;

    /**
     * 备注
     */
    @Column(name = "memo", length = 255)
    private String memo;
}












