package org.example.accounting.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 报表相关 DTO 定义。
 */
public class ReportDtos {

    /**
     * 资产负债表项目 DTO。
     */
    @Data
    public static class BalanceSheetItem {
        private String accountCode;
        private String accountName;
        private String accountType;
        private BigDecimal amount;
    }

    /**
     * 资产负债表响应 DTO。
     */
    @Data
    public static class BalanceSheetResponse {
        private List<BalanceSheetItem> assets;
        private List<BalanceSheetItem> liabilities;
        private List<BalanceSheetItem> equity;
    }

    /**
     * 损益表项目 DTO。
     */
    @Data
    public static class IncomeStatementItem {
        private String accountCode;
        private String accountName;
        private String accountType;
        private BigDecimal amount;
    }

    /**
     * 损益表响应 DTO。
     */
    @Data
    public static class IncomeStatementResponse {
        private List<IncomeStatementItem> incomes;
        private List<IncomeStatementItem> expenses;
    }

    /**
     * 试算平衡表行 DTO。
     */
    @Data
    public static class TrialBalanceRow {
        private String accountCode;
        private String accountName;
        private BigDecimal debit;
        private BigDecimal credit;
    }

    /**
     * 试算平衡表响应 DTO。
     */
    @Data
    public static class TrialBalanceResponse {
        private List<TrialBalanceRow> rows;
        private BigDecimal totalDebit;
        private BigDecimal totalCredit;
    }

    /**
     * 今日汇总（首页使用）：包含资产/负债/权益汇总与当日收支。
     */
    @Data
    public static class TodaySummaryResponse {
        private BigDecimal assetsTotal;
        private BigDecimal liabilitiesTotal;
        private BigDecimal equityTotal;
        private BigDecimal todayIncome;
        private BigDecimal todayExpense;
    }
}







