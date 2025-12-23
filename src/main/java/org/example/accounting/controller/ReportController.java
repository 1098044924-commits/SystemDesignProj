package org.example.accounting.controller;

import org.example.accounting.dto.ReportDtos.BalanceSheetResponse;
import org.example.accounting.dto.ReportDtos.IncomeStatementResponse;
import org.example.accounting.dto.ReportDtos.TrialBalanceResponse;
import org.example.accounting.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报表 REST 控制器。
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 资产负债表。
     */
    @GetMapping("/balance-sheet")
    public BalanceSheetResponse balanceSheet() {
        return reportService.balanceSheet();
    }

    /**
     * 损益表。
     */
    @GetMapping("/income-statement")
    public IncomeStatementResponse incomeStatement() {
        return reportService.incomeStatement();
    }

    /**
     * 试算平衡表。
     */
    @GetMapping("/trial-balance")
    public TrialBalanceResponse trialBalance() {
        return reportService.trialBalance();
    }

    /**
     * 今日汇总：资产/负债/所有者权益与当日收支（用于首页显示）。
     */
    @GetMapping("/today-summary")
    public org.example.accounting.dto.ReportDtos.TodaySummaryResponse todaySummary() {
        return reportService.todaySummary();
    }
}







