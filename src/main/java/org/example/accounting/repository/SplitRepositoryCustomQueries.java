package org.example.accounting.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.example.accounting.domain.Split;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 扩展的分录查询接口，包含按日期范围聚合的查询方法。
 */
public interface SplitRepositoryCustomQueries extends JpaRepository<Split, Long> {

    /**
     * 按账户集合和交易日期范围查询分录。
     */
    @Query("select s from Split s where s.account.id in :accountIds "
            + "and (:start is null or s.transaction.tradeDate >= :start) "
            + "and (:end is null or s.transaction.tradeDate < :end)")
    List<Split> findByAccountIdInAndTransactionTradeDateBetween(
            @Param("accountIds") Collection<Long> accountIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 按交易日期范围查询分录。
     */
    @Query("select s from Split s where "
            + "(:start is null or s.transaction.tradeDate >= :start) "
            + "and (:end is null or s.transaction.tradeDate < :end)")
    List<Split> findByTransactionTradeDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}












