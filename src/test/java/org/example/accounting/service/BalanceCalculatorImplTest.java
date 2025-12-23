package org.example.accounting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.accounting.domain.Account;
import org.example.accounting.domain.AccountType;
import org.example.accounting.domain.Commodity;
import org.example.accounting.domain.DebitCredit;
import org.example.accounting.domain.Split;
import org.example.accounting.domain.Transaction;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.repository.SplitRepository;
import org.example.accounting.service.impl.BalanceCalculatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * BalanceCalculatorImpl 单元测试。
 */
class BalanceCalculatorImplTest {

    private SplitRepository splitRepository;
    private AccountRepository accountRepository;
    private BalanceCalculatorImpl calculator;

    @BeforeEach
    void setUp() {
        splitRepository = Mockito.mock(SplitRepository.class);
        accountRepository = Mockito.mock(AccountRepository.class);
        calculator = new BalanceCalculatorImpl(splitRepository, accountRepository);
    }

    @Test
    void calculateBalance_shouldSumDebitsMinusCredits() {
        LocalDateTime now = LocalDateTime.now();
        Commodity c = Commodity.builder().id(1L).symbol("CNY").name("CNY").fraction(2).type(null).build();
        Account acc = Account.builder().id(1L).code("1001").name("现金").type(AccountType.ASSET)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();

        when(accountRepository.findByParentId(1L)).thenReturn(List.of());

        Transaction txn = Transaction.builder().id(1L).tradeDate(now).cleared(false).build();

        Split s1 = Split.builder().id(1L).transaction(txn).account(acc)
                .amount(new BigDecimal("100.00")).direction(DebitCredit.DEBIT).build();
        Split s2 = Split.builder().id(2L).transaction(txn).account(acc)
                .amount(new BigDecimal("40.00")).direction(DebitCredit.CREDIT).build();

        when(splitRepository.findByAccountIdInAndTransactionTradeDateBetween(
                List.of(1L), null, now)).thenReturn(List.of(s1, s2));

        BigDecimal balance = calculator.calculateBalance(1L, null, now, false);
        assertEquals(new BigDecimal("60.00"), balance);
    }
}


