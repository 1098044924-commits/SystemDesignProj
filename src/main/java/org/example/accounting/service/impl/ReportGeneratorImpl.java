package org.example.accounting.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.accounting.domain.Account;
import org.example.accounting.domain.AccountType;
import org.example.accounting.domain.DebitCredit;
import org.example.accounting.domain.Split;
import org.example.accounting.dto.ReportDtos.BalanceSheetItem;
import org.example.accounting.dto.ReportDtos.BalanceSheetResponse;
import org.example.accounting.dto.ReportDtos.IncomeStatementItem;
import org.example.accounting.dto.ReportDtos.IncomeStatementResponse;
import org.example.accounting.dto.ReportDtos.TrialBalanceResponse;
import org.example.accounting.dto.ReportDtos.TrialBalanceRow;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.repository.SplitRepository;
import org.example.accounting.service.BalanceCalculator;
import org.example.accounting.service.ReportGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 报表生成器实现
 * 基于 BalanceCalculator 和分录记录生成三大报表。
 */
@Service
public class ReportGeneratorImpl implements ReportGenerator {

    private final AccountRepository accountRepository;
    private final SplitRepository splitRepository;
    private final BalanceCalculator balanceCalculator;

    public ReportGeneratorImpl(AccountRepository accountRepository,
            SplitRepository splitRepository,
            BalanceCalculator balanceCalculator) {
        this.accountRepository = accountRepository;
        this.splitRepository = splitRepository;
        this.balanceCalculator = balanceCalculator;
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceSheetResponse generateBalanceSheet(LocalDateTime asOf) {
        // 为保持与“账户余额”模块一致，优先使用 Account.entity.balance（管理员可通过调整余额修改）
        // 并对父账户做子账户余额汇总。避免仅依赖基于分录的动态计算导致与手工调整不一致。
        List<Account> accounts = accountRepository.findAll();
        List<BalanceSheetItem> assets = new ArrayList<>();
        List<BalanceSheetItem> liabilities = new ArrayList<>();
        List<BalanceSheetItem> equity = new ArrayList<>();

        // 构建 parentId -> children 列表，便于在内存中递归汇总
        Map<Long, List<Account>> childrenByParent = new HashMap<>();
        for (Account acc : accounts) {
            Long pid = acc.getParent() != null ? acc.getParent().getId() : null;
            if (pid != null) {
                childrenByParent.computeIfAbsent(pid, k -> new ArrayList<>()).add(acc);
            }
        }

        // 递归计算每个账户及其子账户的汇总余额（使用 account.balance 字段）
        Map<Long, BigDecimal> aggregatedBalanceByAccount = new HashMap<>();
        for (Account acc : accounts) {
            computeAggregatedBalance(acc, childrenByParent, aggregatedBalanceByAccount);
        }

        for (Account account : accounts) {
            BigDecimal balance = aggregatedBalanceByAccount.getOrDefault(account.getId(), BigDecimal.ZERO);

            BalanceSheetItem item = new BalanceSheetItem();
            item.setAccountCode(account.getCode());
            item.setAccountName(account.getName());
            item.setAccountType(account.getType().name());
            item.setAmount(balance);

            if (account.getType() == AccountType.ASSET) {
                assets.add(item);
            } else if (account.getType() == AccountType.LIABILITY) {
                liabilities.add(item);
            } else if (account.getType() == AccountType.EQUITY) {
                equity.add(item);
            }
        }

        BalanceSheetResponse response = new BalanceSheetResponse();
        response.setAssets(assets);
        response.setLiabilities(liabilities);
        response.setEquity(equity);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeStatementResponse generateIncomeStatement(LocalDateTime startInclusive,
            LocalDateTime endExclusive) {
        List<Account> accounts = accountRepository.findAll();
        List<IncomeStatementItem> incomes = new ArrayList<>();
        List<IncomeStatementItem> expenses = new ArrayList<>();

        for (Account account : accounts) {
            if (account.getType() != AccountType.INCOME && account.getType() != AccountType.EXPENSE) {
                continue;
            }
            BigDecimal amount = balanceCalculator
                    .calculateBalance(account.getId(), startInclusive, endExclusive, true);

            IncomeStatementItem item = new IncomeStatementItem();
            item.setAccountCode(account.getCode());
            item.setAccountName(account.getName());
            item.setAccountType(account.getType().name());
            item.setAmount(amount);

            if (account.getType() == AccountType.INCOME) {
                incomes.add(item);
            } else {
                expenses.add(item);
            }
        }

        IncomeStatementResponse response = new IncomeStatementResponse();
        response.setIncomes(incomes);
        response.setExpenses(expenses);
        return response;
    }

    /**
     * 递归计算指定账户的汇总余额（使用 Account.balance 字段），并缓存结果到 aggregatedBalanceByAccount 中。
     */
    private BigDecimal computeAggregatedBalance(Account account, Map<Long, List<Account>> childrenByParent,
            Map<Long, BigDecimal> aggregatedBalanceByAccount) {
        if (aggregatedBalanceByAccount.containsKey(account.getId())) {
            return aggregatedBalanceByAccount.get(account.getId());
        }
        BigDecimal total = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        List<Account> children = childrenByParent.get(account.getId());
        if (children != null && !children.isEmpty()) {
            for (Account child : children) {
                total = total.add(computeAggregatedBalance(child, childrenByParent, aggregatedBalanceByAccount));
            }
        }
        aggregatedBalanceByAccount.put(account.getId(), total);
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public TrialBalanceResponse generateTrialBalance(LocalDateTime startInclusive,
            LocalDateTime endExclusive) {
        List<Split> splits = splitRepository
                .findByTransactionTradeDateBetween(startInclusive, endExclusive);

        Map<Long, TrialBalanceRow> rowsByAccount = new HashMap<>();
        for (Split split : splits) {
            Long accountId = split.getAccount().getId();
            TrialBalanceRow row = rowsByAccount.computeIfAbsent(accountId, id -> {
                TrialBalanceRow r = new TrialBalanceRow();
                r.setAccountCode(split.getAccount().getCode());
                r.setAccountName(split.getAccount().getName());
                r.setDebit(BigDecimal.ZERO);
                r.setCredit(BigDecimal.ZERO);
                return r;
            });

            if (split.getDirection() == DebitCredit.DEBIT) {
                row.setDebit(row.getDebit().add(split.getAmount()));
            } else {
                row.setCredit(row.getCredit().add(split.getAmount()));
            }
        }

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<TrialBalanceRow> rows = new ArrayList<>(rowsByAccount.values());
        for (TrialBalanceRow row : rows) {
            totalDebit = totalDebit.add(row.getDebit());
            totalCredit = totalCredit.add(row.getCredit());
        }

        TrialBalanceResponse response = new TrialBalanceResponse();
        response.setRows(rows);
        response.setTotalDebit(totalDebit);
        response.setTotalCredit(totalCredit);
        return response;
    }
}





