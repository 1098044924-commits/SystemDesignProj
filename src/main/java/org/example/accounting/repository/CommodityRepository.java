package org.example.accounting.repository;

import java.util.Optional;
import org.example.accounting.domain.Commodity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 商品/服务/货币仓储接口。
 */
public interface CommodityRepository extends JpaRepository<Commodity, Long> {

    /**
     * 根据符号查找商品，例如：CNY、USD、AAPL。
     *
     * @param symbol 符号
     * @return 商品
     */
    Optional<Commodity> findBySymbol(String symbol);
}












