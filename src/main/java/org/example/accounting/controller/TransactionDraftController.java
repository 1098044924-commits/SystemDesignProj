package org.example.accounting.controller;

import org.example.accounting.domain.TransactionDraft;
import org.example.accounting.dto.TransactionDtos.CreateTransactionRequest;
import org.example.accounting.dto.TransactionDtos.TransactionResponse;
import org.example.accounting.service.TransactionDraftService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions/drafts")
public class TransactionDraftController {

    private final TransactionDraftService draftService;

    public TransactionDraftController(TransactionDraftService draftService) {
        this.draftService = draftService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TransactionDraft> listDrafts() {
        return draftService.listAllDrafts();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TransactionDraft getDraft(@PathVariable Long id) {
        return draftService.getDraft(id);
    }

    /**
     * 管理员从草稿创建正式交易：POST /api/transactions/drafts/{id}/create
     * Body: CreateTransactionRequest（分录由管理员填写）
     */
    @PostMapping("/{id}/create")
    @PreAuthorize("hasRole('ADMIN')")
    public TransactionResponse createFromDraft(@PathVariable Long id, @RequestBody CreateTransactionRequest req) {
        return draftService.createTransactionFromDraft(id, req);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDraft(@PathVariable Long id) {
        draftService.deleteDraft(id);
    }
}


