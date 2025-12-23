package org.example.accounting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.accounting.domain.Account;
import org.example.accounting.domain.AccountType;
import org.example.accounting.domain.Commodity;
import org.example.accounting.dto.ReportDtos.BalanceSheetResponse;
import org.example.accounting.dto.ReportDtos.IncomeStatementResponse;
import org.example.accounting.dto.ReportDtos.TrialBalanceResponse;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.repository.SplitRepository;
import org.example.accounting.service.impl.ReportGeneratorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * ReportGeneratorImpl 单元测试。
 */
class ReportGeneratorImplTest {

    private AccountRepository accountRepository;
    private SplitRepository splitRepository;
    private BalanceCalculator balanceCalculator;
    private ReportGeneratorImpl generator;

    @BeforeEach
    void setUp() {
        accountRepository = Mockito.mock(AccountRepository.class);
        splitRepository = Mockito.mock(SplitRepository.class);
        balanceCalculator = Mockito.mock(BalanceCalculator.class);
        generator = new ReportGeneratorImpl(accountRepository, splitRepository, balanceCalculator);
    }

    @Test
    void generateBalanceSheet_shouldGroupByAccountType() {
        Commodity c = Commodity.builder().id(1L).symbol("CNY").name("CNY").fraction(2).type(null).build();
        Account asset = Account.builder().id(1L).code("1001").name("现金").type(AccountType.ASSET)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();
        Account liability = Account.builder().id(2L).code("2001").name("应付").type(AccountType.LIABILITY)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();
        when(accountRepository.findAll()).thenReturn(List.of(asset, liability));
        when(balanceCalculator.calculateBalance(Mockito.eq(1L), Mockito.isNull(), Mockito.any(), Mockito.eq(true)))
                .thenReturn(new BigDecimal("100"));
        when(balanceCalculator.calculateBalance(Mockito.eq(2L), Mockito.isNull(), Mockito.any(), Mockito.eq(true)))
                .thenReturn(new BigDecimal("80"));

        BalanceSheetResponse resp = generator.generateBalanceSheet(LocalDateTime.now());
        assertEquals(1, resp.getAssets().size());
        assertEquals(1, resp.getLiabilities().size());
        assertEquals(new BigDecimal("100"), resp.getAssets().get(0).getAmount());
        assertEquals(new BigDecimal("80"), resp.getLiabilities().get(0).getAmount());
    }

    @Test
    void generateIncomeStatement_shouldIncludeIncomeAndExpense() {
        Commodity c = Commodity.builder().id(1L).symbol("CNY").name("CNY").fraction(2).type(null).build();
        Account income = Account.builder().id(1L).code("6001").name("主营业务收入").type(AccountType.INCOME)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();
        Account expense = Account.builder().id(2L).code("6401").name("主营业务成本").type(AccountType.EXPENSE)
                .currency(c).balance(BigDecimal.ZERO).active(true).build();
        when(accountRepository.findAll()).thenReturn(List.of(income, expense));
        when(balanceCalculator.calculateBalance(Mockito.eq(1L), Mockito.any(), Mockito.any(), Mockito.eq(true)))
                .thenReturn(new BigDecimal("500"));
        when(balanceCalculator.calculateBalance(Mockito.eq(2L), Mockito.any(), Mockito.any(), Mockito.eq(true)))
                .thenReturn(new BigDecimal("300"));

        IncomeStatementResponse resp = generator.generateIncomeStatement(
                LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        assertEquals(1, resp.getIncomes().size());
        assertEquals(1, resp.getExpenses().size());
    }

    @Test
    void generateTrialBalance_shouldSumDebitsAndCredits() {
        // 这里主要验证不会抛异常，详细逻辑在 BalanceCalculatorImplTest 已测试
        when(splitRepository.findByTransactionTradeDateBetween(Mockito.any(), Mockito.any()))
                .thenReturn(List.of());
        TrialBalanceResponse resp = generator.generateTrialBalance(
                LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        assertEquals(0, resp.getRows().size());
        assertEquals(BigDecimal.ZERO, resp.getTotalDebit());
        assertEquals(BigDecimal.ZERO, resp.getTotalCredit());
    }
}












