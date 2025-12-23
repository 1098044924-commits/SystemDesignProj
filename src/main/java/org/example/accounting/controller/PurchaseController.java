package org.example.accounting.controller;

import org.example.accounting.domain.PurchaseOrder;
import org.example.accounting.domain.PurchaseOrderAudit;
import org.example.accounting.domain.Supplier;
import org.example.accounting.dto.PurchaseDtos.CreatePurchaseRequest;
import org.example.accounting.dto.PurchaseDtos.CreateSupplierRequest;
import org.example.accounting.service.PurchaseService;
import org.example.accounting.exception.BusinessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/suppliers")
    public List<Supplier> searchSuppliers(@RequestParam(value = "q", required = false) String q) {
        return purchaseService.searchSuppliers(q);
    }

    @PostMapping("/suppliers")
    public Supplier createSupplier(@RequestBody CreateSupplierRequest req) {
        // both admin and employees can create suppliers per spec
        return purchaseService.createSupplier(req);
    }

    @PostMapping("/orders")
    public PurchaseOrder submitOrder(@RequestBody CreatePurchaseRequest req) {
        return purchaseService.submitPurchase(req);
    }

    @PutMapping("/orders/{id}")
    public PurchaseOrder updateOrder(@PathVariable Long id, @RequestBody CreatePurchaseRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (username == null) throw new BusinessException("未登录");
        return purchaseService.updatePurchase(id, req, username);
    }

    @GetMapping("/orders/my")
    public List<PurchaseOrder> myOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (username == null) throw new BusinessException("未登录");
        return purchaseService.listMyOrders(username);
    }

    @GetMapping("/orders/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PurchaseOrder> pendingOrders() {
        return purchaseService.listPending();
    }

    @PutMapping("/orders/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public org.example.accounting.dto.PurchaseDtos.ReviewResult reviewOrder(@PathVariable Long id, @RequestBody ReviewRequest req) {
        return purchaseService.reviewOrder(id, req.isApproved(), req.getReason());
    }

    @GetMapping("/orders/{id}/audits")
    public List<PurchaseOrderAudit> audits(@PathVariable Long id) {
        return purchaseService.listAudits(id);
    }

    @GetMapping("/orders/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PurchaseOrder> allOrders() {
        return purchaseService.listAllOrders();
    }

    // small DTO for review payload
    public static class ReviewRequest {
        private boolean approved;
        private String reason;
        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}


