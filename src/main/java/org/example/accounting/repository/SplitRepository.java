package org.example.accounting.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.example.accounting.domain.Split;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 分录仓储接口。
 */
public interface SplitRepository extends JpaRepository<Split, Long> {

    /**
     * 根据交易 ID 查询分录列表。
     *
     * @param transactionId 交易 ID
     * @return 分录列表
     */
    List<Split> findByTransactionId(Long transactionId);

    /**
     * 按账户集合和交易日期范围查询分录。
     *
     * @param accountIds 账户 ID 集合
     * @param start      起始时间（可为空）
     * @param end        截止时间（可为空）
     * @return 分录列表
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
     *
     * @param start 起始时间（可为空）
     * @param end   截止时间（可为空）
     * @return 分录列表
     */
    @Query("select s from Split s where "
            + "(:start is null or s.transaction.tradeDate >= :start) "
            + "and (:end is null or s.transaction.tradeDate < :end)")
    List<Split> findByTransactionTradeDateBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * 检查账户是否有交易记录（分录）。
     *
     * @param accountId 账户 ID
     * @return 是否存在交易记录
     */
    boolean existsByAccountId(Long accountId);
}

