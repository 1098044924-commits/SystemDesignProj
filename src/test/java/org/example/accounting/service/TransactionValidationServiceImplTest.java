package org.example.accounting.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.example.accounting.domain.Account;
import org.example.accounting.domain.AccountType;
import org.example.accounting.domain.Commodity;
import org.example.accounting.dto.TransactionDtos.CreateTransactionRequest;
import org.example.accounting.dto.TransactionDtos.SplitCreateRequest;
import org.example.accounting.exception.BusinessException;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.service.impl.TransactionValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * TransactionValidationServiceImpl 单元测试。
 */
class TransactionValidationServiceImplTest {

    private AccountRepository accountRepository;
    private TransactionValidationServiceImpl service;

    @BeforeEach
    void setUp() {
        accountRepository = Mockito.mock(AccountRepository.class);
        service = new TransactionValidationServiceImpl(accountRepository);
    }

    @Test
    void validateNewTransaction_ok_whenBalancedAndAccountsValid() {
        CreateTransactionRequest req = new CreateTransactionRequest();
        SplitCreateRequest s1 = new SplitCreateRequest();
        s1.setAccountId(1L);
        s1.setAmount(new BigDecimal("100.00"));
        s1.setDirection("DEBIT");
        SplitCreateRequest s2 = new SplitCreateRequest();
        s2.setAccountId(2L);
        s2.setAmount(new BigDecimal("100.00"));
        s2.setDirection("CREDIT");
        req.setSplits(java.util.List.of(s1, s2));

        Commodity c = Commodity.builder().id(10L).symbol("CNY").type(null).fraction(2).name("CNY").build();
        Account a1 = Account.builder().id(1L).code("1001").name("现金").type(AccountType.ASSET)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();
        Account a2 = Account.builder().id(2L).code("2001").name("应付").type(AccountType.LIABILITY)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(a1));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(a2));

        assertDoesNotThrow(() -> service.validateNewTransaction(req));
    }

    @Test
    void validateNewTransaction_throws_whenUnbalanced() {
        CreateTransactionRequest req = new CreateTransactionRequest();
        SplitCreateRequest s1 = new SplitCreateRequest();
        s1.setAccountId(1L);
        s1.setAmount(new BigDecimal("100.00"));
        s1.setDirection("DEBIT");
        SplitCreateRequest s2 = new SplitCreateRequest();
        s2.setAccountId(2L);
        s2.setAmount(new BigDecimal("50.00"));
        s2.setDirection("CREDIT");
        req.setSplits(java.util.List.of(s1, s2));

        Commodity c = Commodity.builder().id(10L).symbol("CNY").type(null).fraction(2).name("CNY").build();
        Account a1 = Account.builder().id(1L).code("1001").name("现金").type(AccountType.ASSET)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();
        Account a2 = Account.builder().id(2L).code("2001").name("应付").type(AccountType.LIABILITY)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(a1));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(a2));

        assertThrows(BusinessException.class, () -> service.validateNewTransaction(req));
    }
}












