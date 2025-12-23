package org.example.accounting.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.accounting.domain.Transaction;
import org.example.accounting.exception.BusinessException;
import org.example.accounting.repository.TransactionRepository;
import org.example.accounting.service.ReconciliationReport;
import org.example.accounting.service.ReconciliationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对账服务实现
 * 基于交易的 cleared 标记完成简单的对账逻辑。
 */
@Service
public class ReconciliationServiceImpl implements ReconciliationService {

    private final TransactionRepository transactionRepository;

    public ReconciliationServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void markTransactionCleared(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("交易不存在: " + transactionId));
        transaction.setCleared(true);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public ReconciliationReport generateReport(LocalDate startInclusive, LocalDate endInclusive) {
        LocalDateTime start = startInclusive.atStartOfDay();
        LocalDateTime end = endInclusive.plusDays(1).atStartOfDay();

        List<Transaction> all = transactionRepository.findAll().stream()
                .filter(t -> !t.getTradeDate().isBefore(start)
                        && t.getTradeDate().isBefore(end))
                .toList();

        List<Transaction> cleared = all.stream().filter(Transaction::getCleared).toList();
        List<Transaction> uncleared = all.stream().filter(t -> !t.getCleared()).toList();

        BigDecimal clearedTotal = cleared.stream()
                .flatMap(t -> t.getSplits().stream())
                .map(s -> s.getAmount() == null ? BigDecimal.ZERO : s.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unclearedTotal = uncleared.stream()
                .flatMap(t -> t.getSplits().stream())
                .map(s -> s.getAmount() == null ? BigDecimal.ZERO : s.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReconciliationReport report = new ReconciliationReport();
        report.setClearedTransactions(cleared);
        report.setUnclearedTransactions(uncleared);
        report.setClearedTotal(clearedTotal);
        report.setUnclearedTotal(unclearedTotal);
        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findOutstandingItems(LocalDate upToDate) {
        LocalDateTime end = upToDate.plusDays(1).atStartOfDay();
        return transactionRepository.findByCleared(false, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(t -> t.getTradeDate().isBefore(end))
                .toList();
    }
}












