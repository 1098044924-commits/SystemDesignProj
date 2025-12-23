package org.example.accounting.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 商品/服务/货币实体
 * 对应数据库中的 commodities 表。
 */
@Entity
@Table(name = "commodities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commodity {

    /**
     * 主键 ID，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 符号/代码，例如：CNY、USD、AAPL
     */
    @Column(name = "symbol", nullable = false, length = 50, unique = true)
    private String symbol;

    /**
     * 名称，例如：人民币、美元、苹果股票
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 商品/服务类型：货币、股票、基金、商品等
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private CommodityType type;

    /**
     * 小数位数，用于金额或数量精度控制
     */
    @Column(name = "fraction", nullable = false)
    private Integer fraction;
}












