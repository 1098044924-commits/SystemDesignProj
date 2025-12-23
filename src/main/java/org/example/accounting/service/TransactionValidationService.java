package org.example.accounting.service;

import org.example.accounting.dto.TransactionDtos.CreateTransactionRequest;

/**
 * 交易验证服务
 * 负责校验双式记账交易的核心规则：
 * 1. 借贷金额是否平衡；
 * 2. 账户是否存在且未关闭；
 * 3. 货币是否一致。
 */
public interface TransactionValidationService {

    /**
     * 校验新建交易请求是否符合双式记账规则。
     *
     * @param request 交易创建请求
     */
    void validateNewTransaction(CreateTransactionRequest request);
}












