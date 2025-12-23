package org.example.accounting.domain;

/**
 * 借贷方向枚举
 * 每条分录必须标记为借方或贷方，用于双式记账。
 */
public enum DebitCredit {

    /**
     * 借方
     */
    DEBIT,

    /**
     * 贷方
     */
    CREDIT
}












