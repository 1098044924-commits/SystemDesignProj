package org.example.accounting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.accounting.domain.Split;
import org.example.accounting.domain.Transaction;
import org.example.accounting.repository.TransactionRepository;
import org.example.accounting.service.impl.ReconciliationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * ReconciliationServiceImpl 单元测试。
 */
class ReconciliationServiceImplTest {

    private TransactionRepository transactionRepository;
    private ReconciliationServiceImpl service;

    @BeforeEach
    void setUp() {
        transactionRepository = Mockito.mock(TransactionRepository.class);
        service = new ReconciliationServiceImpl(transactionRepository);
    }

    @Test
    void generateReport_shouldSplitClearedAndUncleared() {
        LocalDateTime now = LocalDateTime.now();
        Transaction t1 = Transaction.builder().id(1L).tradeDate(now.minusDays(1)).cleared(true).build();
        Transaction t2 = Transaction.builder().id(2L).tradeDate(now.minusDays(1)).cleared(false).build();

        Split s1 = Split.builder().amount(new BigDecimal("100")).transaction(t1).build();
        Split s2 = Split.builder().amount(new BigDecimal("50")).transaction(t2).build();
        t1.setSplits(List.of(s1));
        t2.setSplits(List.of(s2));

        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2));

        ReconciliationReport report = service.generateReport(
                LocalDate.now().minusDays(2), LocalDate.now());

        assertEquals(1, report.getClearedTransactions().size());
        assertEquals(1, report.getUnclearedTransactions().size());
        assertEquals(new BigDecimal("100"), report.getClearedTotal());
        assertEquals(new BigDecimal("50"), report.getUnclearedTotal());
    }

    @Test
    void findOutstandingItems_shouldReturnUnclearedBeforeDate() {
        LocalDateTime now = LocalDateTime.now();
        Transaction t1 = Transaction.builder().id(1L).tradeDate(now.minusDays(2)).cleared(false).build();
        Transaction t2 = Transaction.builder().id(2L).tradeDate(now.plusDays(1)).cleared(false).build();
        Page<Transaction> page = Page.empty();

        when(transactionRepository.findByCleared(false, Pageable.unpaged()))
                .thenReturn(page.map(x -> x));
        when(transactionRepository.findByCleared(false, Pageable.unpaged()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(t1, t2)));

        List<Transaction> result = service.findOutstandingItems(LocalDate.now());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}


