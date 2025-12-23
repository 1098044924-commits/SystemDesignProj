package org.example.accounting.service;

import java.time.LocalDateTime;
import org.example.accounting.dto.ReportDtos.BalanceSheetResponse;
import org.example.accounting.dto.ReportDtos.IncomeStatementResponse;
import org.example.accounting.dto.ReportDtos.TrialBalanceResponse;

/**
 * 报表生成器
 * 负责根据日期范围生成资产负债表、损益表和试算平衡表。
 */
public interface ReportGenerator {

    /**
     * 生成指定时点的资产负债表。
     *
     * @param asOf 截止时间
     * @return 资产负债表
     */
    BalanceSheetResponse generateBalanceSheet(LocalDateTime asOf);

    /**
     * 生成指定期间的损益表。
     *
     * @param startInclusive 起始时间（包含）
     * @param endExclusive   截止时间（不包含）
     * @return 损益表
     */
    IncomeStatementResponse generateIncomeStatement(LocalDateTime startInclusive,
            LocalDateTime endExclusive);

    /**
     * 生成指定期间的试算平衡表。
     *
     * @param startInclusive 起始时间（包含）
     * @param endExclusive   截止时间（不包含）
     * @return 试算平衡表
     */
    TrialBalanceResponse generateTrialBalance(LocalDateTime startInclusive,
            LocalDateTime endExclusive);
}












