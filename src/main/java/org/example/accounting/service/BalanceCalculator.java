package org.example.accounting.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额计算服务
 * 负责在指定日期范围内计算账户余额，并支持父账户递归汇总。
 */
public interface BalanceCalculator {

    /**
     * 计算指定账户在给定时间区间内的余额。
     *
     * @param accountId        账户 ID
     * @param startInclusive   起始时间（包含），可为空表示无限制
     * @param endExclusive     截止时间（不包含），可为空表示当前
     * @param includeChildren  是否包含子账户
     * @return 余额
     */
    BigDecimal calculateBalance(Long accountId, LocalDateTime startInclusive,
            LocalDateTime endExclusive, boolean includeChildren);
}












