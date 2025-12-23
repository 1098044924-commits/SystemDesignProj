package org.example.accounting.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

/**
 * 供应商实体
 */
@Entity
@Table(name = "suppliers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 200)
    private String location;

    @Column(length = 200)
    private String email;

    @Column(name = "product_name", length = 150)
    private String productName;

    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;
}






