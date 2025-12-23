package org.example.accounting.service;

import java.time.LocalDate;
import java.util.List;
import org.example.accounting.domain.Transaction;

/**
 * 对账服务
 * 负责标记交易为已核对、生成对账报告以及处理未达账项。
 */
public interface ReconciliationService {

    /**
     * 将一笔交易标记为已核对。
     *
     * @param transactionId 交易 ID
     */
    void markTransactionCleared(Long transactionId);

    /**
     * 生成某一日期范围内的对账报告。
     *
     * @param startInclusive 起始日期（包含）
     * @param endInclusive   截止日期（包含）
     * @return 对账报告 DTO
     */
    ReconciliationReport generateReport(LocalDate startInclusive, LocalDate endInclusive);

    /**
     * 查找指定日期之前的未达账项（未核对的交易）。
     *
     * @param upToDate 截止日期（包含）
     * @return 未达账项交易列表
     */
    List<Transaction> findOutstandingItems(LocalDate upToDate);
}












