package org.example.accounting.service.impl;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.example.accounting.domain.Account;
import org.example.accounting.domain.DebitCredit;
import org.example.accounting.dto.TransactionDtos.CreateTransactionRequest;
import org.example.accounting.dto.TransactionDtos.SplitCreateRequest;
import org.example.accounting.exception.BusinessException;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.service.TransactionValidationService;
import org.springframework.stereotype.Service;

/**
 * 交易验证服务实现
 * 负责校验借贷平衡、账户有效性以及货币一致性。
 */
@Service
public class TransactionValidationServiceImpl implements TransactionValidationService {

    private final AccountRepository accountRepository;

    public TransactionValidationServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void validateNewTransaction(CreateTransactionRequest request) {
        if (request.getSplits() == null || request.getSplits().size() < 2) {
            throw new BusinessException("双式记账交易至少需要两条分录");
        }

        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;
        Set<Long> currencyIds = new HashSet<>();

        for (SplitCreateRequest split : request.getSplits()) {
            if (split.getAccountId() == null) {
                throw new BusinessException("分录账户不能为空");
            }
            if (split.getAmount() == null) {
                throw new BusinessException("分录金额不能为空");
            }

            Account account = accountRepository.findById(split.getAccountId())
                    .orElseThrow(() -> new BusinessException("账户不存在: " + split.getAccountId()));

            if (Boolean.FALSE.equals(account.getActive())) {
                throw new BusinessException("账户已关闭，不能继续记账: " + account.getCode());
            }

            currencyIds.add(account.getCurrency().getId());

            DebitCredit direction;
            try {
                direction = DebitCredit.valueOf(split.getDirection());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("非法借贷方向: " + split.getDirection());
            }

            if (direction == DebitCredit.DEBIT) {
                debitTotal = debitTotal.add(split.getAmount());
            } else {
                creditTotal = creditTotal.add(split.getAmount());
            }
        }

        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new BusinessException("借贷不平衡：借方合计=" + debitTotal + "，贷方合计=" + creditTotal);
        }

        if (currencyIds.size() > 1) {
            throw new BusinessException("同一笔交易的所有账户必须使用相同的货币");
        }
    }
}












