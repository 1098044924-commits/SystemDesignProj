package org.example.accounting.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 账户/科目实体
 * 支持树形结构，并记录账户类型、货币与当前余额。
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    /**
     * 主键 ID，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 账户名称，例如：现金、银行存款
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 科目代码，例如：1010.01
     */
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    /**
     * 账户类型：资产、负债、权益、收入、费用
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AccountType type;

    /**
     * 当前余额，单位为关联货币
     */
    @Column(name = "balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    /**
     * 账户是否有效/启用
     * 如果为 false，视为已关闭账户，不允许继续发生新交易。
     */
    @Column(name = "active", nullable = false)
    private Boolean active;

    /**
     * 账户使用的货币
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Commodity currency;

    /**
     * 父账户（树形结构）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Account parent;

    /**
     * 子账户列表
     */
    @OneToMany(mappedBy = "parent")
    private List<Account> children;
}


