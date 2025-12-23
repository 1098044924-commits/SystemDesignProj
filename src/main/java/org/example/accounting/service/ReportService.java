package org.example.accounting.service;

import org.example.accounting.dto.ReportDtos.BalanceSheetItem;
import org.example.accounting.dto.ReportDtos.BalanceSheetResponse;
import org.example.accounting.dto.ReportDtos.IncomeStatementItem;
import org.example.accounting.dto.ReportDtos.IncomeStatementResponse;
import org.example.accounting.dto.ReportDtos.TrialBalanceResponse;
import org.example.accounting.dto.ReportDtos.TrialBalanceRow;
import org.example.accounting.service.ReportGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 报表服务层
 * 提供资产负债表、损益表以及试算平衡表的基础实现。
 * 这里实现的是简化版逻辑，后续可以根据具体会计规则进一步细化。
 */
@Service
public class ReportService {

    private final ReportGenerator reportGenerator;

    public ReportService(ReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    /**
     * 生成简化版资产负债表。
     */
    @Transactional(readOnly = true)
    public BalanceSheetResponse balanceSheet() {
        return reportGenerator.generateBalanceSheet(java.time.LocalDateTime.now());
    }

    /**
     * 生成简化版损益表。
     */
    @Transactional(readOnly = true)
    public IncomeStatementResponse incomeStatement() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime start = now.minusMonths(1);
        return reportGenerator.generateIncomeStatement(start, now);
    }

    /**
     * 生成试算平衡表。
     * 根据所有分录按账户聚合，统计借方和贷方金额。
     */
    @Transactional(readOnly = true)
    public TrialBalanceResponse trialBalance() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime start = now.minusMonths(1);
        return reportGenerator.generateTrialBalance(start, now);
    }

    /**
     * 今日汇总，用于首页显示资产/负债/权益以及当日收支统计。
     */
    @Transactional(readOnly = true)
    public org.example.accounting.dto.ReportDtos.TodaySummaryResponse todaySummary() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        // balance sheet as of now
        BalanceSheetResponse bs = reportGenerator.generateBalanceSheet(now);
        // income statement for today
        IncomeStatementResponse is = reportGenerator.generateIncomeStatement(startOfDay, now);

        java.math.BigDecimal assetsTotal = bs.getAssets() == null ? java.math.BigDecimal.ZERO
                : bs.getAssets().stream().map(i -> i.getAmount() == null ? java.math.BigDecimal.ZERO : i.getAmount())
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal liabilitiesTotal = bs.getLiabilities() == null ? java.math.BigDecimal.ZERO
                : bs.getLiabilities().stream().map(i -> i.getAmount() == null ? java.math.BigDecimal.ZERO : i.getAmount())
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal equityTotal = bs.getEquity() == null ? java.math.BigDecimal.ZERO
                : bs.getEquity().stream().map(i -> i.getAmount() == null ? java.math.BigDecimal.ZERO : i.getAmount())
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal todayIncome = java.math.BigDecimal.ZERO;
        java.math.BigDecimal todayExpense = java.math.BigDecimal.ZERO;
        if (is != null) {
            if (is.getIncomes() != null) {
                todayIncome = is.getIncomes().stream().map(i -> i.getAmount() == null ? java.math.BigDecimal.ZERO : i.getAmount())
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            }
            if (is.getExpenses() != null) {
                todayExpense = is.getExpenses().stream().map(i -> i.getAmount() == null ? java.math.BigDecimal.ZERO : i.getAmount())
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            }
        }

        org.example.accounting.dto.ReportDtos.TodaySummaryResponse resp = new org.example.accounting.dto.ReportDtos.TodaySummaryResponse();
        resp.setAssetsTotal(assetsTotal);
        resp.setLiabilitiesTotal(liabilitiesTotal);
        resp.setEquityTotal(equityTotal);
        resp.setTodayIncome(todayIncome);
        resp.setTodayExpense(todayExpense);
        return resp;
    }
}


