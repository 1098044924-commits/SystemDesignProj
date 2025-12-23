package org.example.accounting.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.example.accounting.domain.DebitCredit;
import org.example.accounting.domain.Split;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.repository.SplitRepository;
import org.example.accounting.service.BalanceCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 余额计算服务实现
 * 使用分录记录在指定日期范围内动态计算账户余额。
 * 简单实现了一个内存级缓存，并在说明中约定在新交易落账后清理缓存。
 */
@Service
public class BalanceCalculatorImpl implements BalanceCalculator {

    private final SplitRepository splitRepository;
    private final AccountRepository accountRepository;

    /**
     * 简单缓存：key = accountId|start|end|includeChildren，value = 余额。
     */
    private final ConcurrentMap<String, BigDecimal> cache = new ConcurrentHashMap<>();

    public BalanceCalculatorImpl(SplitRepository splitRepository,
            AccountRepository accountRepository) {
        this.splitRepository = splitRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(Long accountId, LocalDateTime startInclusive,
            LocalDateTime endExclusive, boolean includeChildren) {
        String key = buildKey(accountId, startInclusive, endExclusive, includeChildren);
        return cache.computeIfAbsent(key,
                k -> doCalculate(accountId, startInclusive, endExclusive, includeChildren));
    }

    private BigDecimal doCalculate(Long accountId, LocalDateTime startInclusive,
            LocalDateTime endExclusive, boolean includeChildren) {
        List<Long> accountIds = new ArrayList<>();
        accountIds.add(accountId);
        if (includeChildren) {
            collectChildren(accountId, accountIds);
        }
        List<Split> splits = splitRepository
                .findByAccountIdInAndTransactionTradeDateBetween(accountIds, startInclusive,
                        endExclusive);

        BigDecimal balance = BigDecimal.ZERO;
        for (Split split : splits) {
            if (split.getDirection() == DebitCredit.DEBIT) {
                balance = balance.add(split.getAmount());
            } else {
                balance = balance.subtract(split.getAmount());
            }
        }
        return balance;
    }

    private void collectChildren(Long parentId, List<Long> collector) {
        accountRepository.findByParentId(parentId).forEach(child -> {
            collector.add(child.getId());
            collectChildren(child.getId(), collector);
        });
    }

    private String buildKey(Long accountId, LocalDateTime startInclusive,
            LocalDateTime endExclusive, boolean includeChildren) {
        return accountId + "|" + (startInclusive == null ? "null" : startInclusive)
                + "|" + (endExclusive == null ? "null" : endExclusive)
                + "|" + includeChildren;
    }

    /**
     * 在新交易创建/更新后调用，清空缓存，保证余额实时性。
     */
    public void clearCache() {
        cache.clear();
    }
}












