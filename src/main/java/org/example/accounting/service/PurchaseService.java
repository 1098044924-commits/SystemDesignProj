package org.example.accounting.service;

import org.example.accounting.domain.PurchaseOrder;
import org.example.accounting.domain.PurchaseStatus;
import org.example.accounting.domain.Supplier;
import org.example.accounting.dto.PurchaseDtos.*;
import org.example.accounting.repository.PurchaseOrderRepository;
import org.example.accounting.repository.PurchaseOrderAuditRepository;
import org.example.accounting.domain.PurchaseOrderAudit;
import org.example.accounting.repository.SupplierRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购服务，处理供应商与采购单的创建/审核等逻辑。
 */
@Service
public class PurchaseService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository orderRepository;
    private final PurchaseOrderAuditRepository auditRepository;
    private final EmailService emailService;
    private final org.example.accounting.service.TransactionDraftService transactionDraftService;

    public PurchaseService(SupplierRepository supplierRepository,
                           PurchaseOrderRepository orderRepository,
                           PurchaseOrderAuditRepository auditRepository,
                           EmailService emailService,
                           org.example.accounting.service.TransactionDraftService transactionDraftService) {
        this.supplierRepository = supplierRepository;
        this.orderRepository = orderRepository;
        this.auditRepository = auditRepository;
        this.emailService = emailService;
        this.transactionDraftService = transactionDraftService;
    }

    @Transactional
    public Supplier createSupplier(CreateSupplierRequest req){
        Supplier s = new Supplier();
        s.setName(req.getName());
        s.setLocation(req.getLocation());
        s.setEmail(req.getEmail());
        s.setProductName(req.getProductName());
        s.setUnitPrice(req.getUnitPrice());
        return supplierRepository.save(s);
    }

    public List<Supplier> searchSuppliers(String q){
        if(q == null || q.isBlank()) return supplierRepository.findAll();
        return supplierRepository.findByNameContainingIgnoreCaseOrProductNameContainingIgnoreCase(q, q);
    }

    @Transactional
    public PurchaseOrder submitPurchase(CreatePurchaseRequest req){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        Supplier s = supplierRepository.findById(req.getSupplierId()).orElse(null);
        PurchaseOrder o = new PurchaseOrder();
        o.setCreatedBy(username);
        o.setCreatedAt(LocalDateTime.now());
        o.setProductName(req.getProductName());
        o.setSupplierId(req.getSupplierId());
        o.setQuantity(req.getQuantity());
        o.setUnitPrice(s != null ? s.getUnitPrice() : null);
        o.setStatus(PurchaseStatus.PENDING);
        return orderRepository.save(o);
    }

    @Transactional
    public PurchaseOrder updatePurchase(Long id, CreatePurchaseRequest req, String username){
        PurchaseOrder o = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        if(!username.equals(o.getCreatedBy())) throw new RuntimeException("Forbidden");
        o.setProductName(req.getProductName());
        o.setSupplierId(req.getSupplierId());
        o.setQuantity(req.getQuantity());
        Supplier s = supplierRepository.findById(req.getSupplierId()).orElse(null);
        o.setUnitPrice(s != null ? s.getUnitPrice() : null);
        o.setStatus(PurchaseStatus.PENDING);
        o.setRejectionReason(null);
        o.setReviewedBy(null);
        o.setReviewedAt(null);
        PurchaseOrder saved = orderRepository.save(o);
        // add audit entry
        PurchaseOrderAudit a = new PurchaseOrderAudit();
        a.setOrderId(saved.getId());
        a.setAction("UPDATED");
        a.setActor(username);
        a.setComment("Employee updated and resubmitted");
        a.setCreatedAt(LocalDateTime.now());
        auditRepository.save(a);
        return saved;
    }

    public List<PurchaseOrder> listMyOrders(String username){
        return orderRepository.findByCreatedBy(username);
    }

    public List<PurchaseOrder> listPending(){
        return orderRepository.findByStatus(PurchaseStatus.PENDING);
    }

    @Transactional
    public org.example.accounting.dto.PurchaseDtos.ReviewResult reviewOrder(Long id, boolean approved, String reason){
        PurchaseOrder o = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String reviewer = auth != null ? auth.getName() : "system";
        if(approved){
            o.setStatus(PurchaseStatus.APPROVED);
            o.setReviewedBy(reviewer);
            o.setReviewedAt(LocalDateTime.now());
            // notify supplier by email if available
            Supplier s = supplierRepository.findById(o.getSupplierId()).orElse(null);
            if(s != null && s.getEmail() != null && !s.getEmail().isBlank()){
                String subject = "采购单通知 - 订单 " + o.getId();
                String body = "尊敬的 " + s.getName() + "，\\n我们已通过采购单，请准备发货。商品：" + o.getProductName() + " 数量：" + o.getQuantity();
                try{ emailService.sendEmail(s.getEmail(), subject, body); }catch(Exception ignored){ }
            }
        } else {
            o.setStatus(PurchaseStatus.REJECTED);
            o.setRejectionReason(reason);
            o.setReviewedBy(reviewer);
            o.setReviewedAt(LocalDateTime.now());
        }
        PurchaseOrder saved = orderRepository.save(o);
        // write audit entry
        PurchaseOrderAudit audit = new PurchaseOrderAudit();
        audit.setOrderId(saved.getId());
        audit.setAction(approved ? "APPROVED" : "REJECTED");
        audit.setActor(reviewer);
        audit.setComment(approved ? "Approved" : reason);
        audit.setCreatedAt(LocalDateTime.now());
        auditRepository.save(audit);

        Long draftTxnId = null;
        // 如果审批通过，自动生成一笔交易草稿，交给管理员在草稿箱中补充科目
        if (approved) {
            try {
                var draft = transactionDraftService.createDraftFromPurchase(saved);
                if (draft != null) draftTxnId = draft.getId();
            } catch (Exception ignored) {
                // 生成草稿失败不影响审批主流程
            }
        }

        org.example.accounting.dto.PurchaseDtos.ReviewResult result = new org.example.accounting.dto.PurchaseDtos.ReviewResult();
        result.setOrder(saved);
        result.setDraftTransactionId(draftTxnId);
        return result;
    }

    public java.util.List<PurchaseOrderAudit> listAudits(Long orderId){
        return auditRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    public java.util.List<PurchaseOrder> listAllOrders(){
        return orderRepository.findAll();
    }
}


