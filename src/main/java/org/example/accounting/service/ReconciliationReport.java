package org.example.accounting.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import org.example.accounting.domain.Transaction;

/**
 * 对账报告 DTO
 * 用于描述某一期间内已核对和未核对交易的汇总信息。
 */
@Data
public class ReconciliationReport {

    /**
     * 已核对交易列表。
     */
    private List<Transaction> clearedTransactions;

    /**
     * 未核对交易列表。
     */
    private List<Transaction> unclearedTransactions;

    /**
     * 已核对交易的金额总计（借方与贷方合计）。
     */
    private BigDecimal clearedTotal;

    /**
     * 未核对交易的金额总计（借方与贷方合计）。
     */
    private BigDecimal unclearedTotal;
}












