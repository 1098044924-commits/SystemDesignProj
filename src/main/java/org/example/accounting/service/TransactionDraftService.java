package org.example.accounting.service;

import org.example.accounting.domain.TransactionDraft;
import org.example.accounting.dto.TransactionDtos.CreateTransactionRequest;
import org.example.accounting.dto.TransactionDtos.TransactionResponse;
import org.example.accounting.domain.PurchaseOrder;
import org.example.accounting.repository.TransactionDraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理交易草稿：创建、查询、并把草稿转换为正式交易。
 */
@Service
public class TransactionDraftService {

    private final TransactionDraftRepository draftRepository;
    private final org.example.accounting.service.TransactionService transactionService;

    public TransactionDraftService(TransactionDraftRepository draftRepository,
                                   org.example.accounting.service.TransactionService transactionService) {
        this.draftRepository = draftRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public TransactionDraft createDraftFromPurchase(PurchaseOrder purchaseOrder) {
        BigDecimal total = BigDecimal.ZERO;
        if (purchaseOrder.getUnitPrice() != null && purchaseOrder.getQuantity() != null) {
            total = purchaseOrder.getUnitPrice().multiply(BigDecimal.valueOf(purchaseOrder.getQuantity()));
        }
        String description = "采购单 #" + purchaseOrder.getId() + " - " + purchaseOrder.getProductName()
                + " 数量:" + purchaseOrder.getQuantity() + " 总额:" + total;

        TransactionDraft d = TransactionDraft.builder()
                .purchaseOrderId(purchaseOrder.getId())
                .tradeDate(LocalDateTime.now())
                .description(description)
                .amount(total)
                .createdBy(purchaseOrder.getReviewedBy() != null ? purchaseOrder.getReviewedBy() : "system")
                .createdAt(LocalDateTime.now())
                .build();
        TransactionDraft saved = draftRepository.save(d);

        // notify admins/global
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("draftId", saved.getId());
            payload.put("purchaseOrderId", saved.getPurchaseOrderId());
            payload.put("description", saved.getDescription());
            // Use NotificationService if available via Spring context
            org.example.accounting.service.NotificationService notif = org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext().getBean(org.example.accounting.service.NotificationService.class);
            if (notif != null) notif.notifyAllUsers("NEW_TRANSACTION_DRAFT", payload);
        } catch (Exception ignored) {}

        return saved;
    }

    @Transactional(readOnly = true)
    public List<TransactionDraft> listAllDrafts() {
        return draftRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TransactionDraft getDraft(Long id) {
        return draftRepository.findById(id).orElseThrow(() -> new RuntimeException("Draft not found"));
    }

    @Transactional
    public TransactionResponse createTransactionFromDraft(Long draftId, CreateTransactionRequest request) {
        // delegate to TransactionService to create actual transaction (will validate)
        TransactionResponse resp = transactionService.createTransaction(request);
        // delete draft after successful creation
        draftRepository.deleteById(draftId);
        return resp;
    }

    @Transactional
    public void deleteDraft(Long id) {
        if (!draftRepository.existsById(id)) {
            throw new RuntimeException("Draft not found: " + id);
        }
        draftRepository.deleteById(id);
    }
}


