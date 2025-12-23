package org.example.accounting.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.accounting.domain.Account;
import org.example.accounting.domain.AccountType;
import org.example.accounting.domain.DebitCredit;
import org.example.accounting.domain.Split;
import org.example.accounting.domain.Transaction;
import org.example.accounting.dto.TransactionDtos.CreateTransactionRequest;
import org.example.accounting.dto.TransactionDtos.SplitCreateRequest;
import org.example.accounting.dto.TransactionDtos.TransactionResponse;
import org.example.accounting.exception.BusinessException;
import org.example.accounting.mapper.TransactionMapper;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.repository.SplitRepository;
import org.example.accounting.repository.TransactionAttachmentRepository;
import org.example.accounting.repository.TransactionRepository;
import org.example.accounting.service.impl.BalanceCalculatorImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 交易服务层
 * 负责创建双式记账交易、分页查询及待核对交易列表。
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SplitRepository splitRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionValidationService validationService;
    private final AccountRepository accountRepository;
    private final org.example.accounting.repository.CommodityRepository commodityRepository;
    private final BalanceCalculatorImpl balanceCalculator;
    private final TransactionAttachmentRepository attachmentRepository;
    private final org.example.accounting.service.NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
            SplitRepository splitRepository,
            AccountRepository accountRepository,
            org.example.accounting.repository.CommodityRepository commodityRepository,
            TransactionMapper transactionMapper,
            TransactionValidationService validationService,
            BalanceCalculatorImpl balanceCalculator,
            TransactionAttachmentRepository attachmentRepository,
            org.example.accounting.service.NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.splitRepository = splitRepository;
        this.accountRepository = accountRepository;
        this.commodityRepository = commodityRepository;
        this.transactionMapper = transactionMapper;
        this.validationService = validationService;
        this.balanceCalculator = balanceCalculator;
        this.attachmentRepository = attachmentRepository;
        this.notificationService = notificationService;
    }

    /**
     * 根据采购单生成一笔草稿交易（不做严格校验），用于管理员在界面上确认或修改科目/分录后再正式保存或审核。
     * 描述与参考号会自动填充，分录会尽量使用系统中可用的“费用/负债”科目作为建议。
     */
    @Transactional
    public TransactionResponse createDraftFromPurchase(org.example.accounting.domain.PurchaseOrder purchaseOrder) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        if (purchaseOrder.getUnitPrice() != null && purchaseOrder.getQuantity() != null) {
            total = purchaseOrder.getUnitPrice().multiply(java.math.BigDecimal.valueOf(purchaseOrder.getQuantity()));
        }

        String description = "采购单 #" + purchaseOrder.getId() + " - " + purchaseOrder.getProductName() 
                + " 数量:" + purchaseOrder.getQuantity() + " 总额:" + total;
        String reference = "PO-" + purchaseOrder.getId();

        String createdBy = purchaseOrder.getReviewedBy() != null ? purchaseOrder.getReviewedBy() : "system";

        Transaction txn = Transaction.builder()
                .tradeDate(LocalDateTime.now())
                .description(description)
                .reference(reference)
                .cleared(false) // 待管理员核对
                .createdBy(createdBy)
                .build();

        // 尝试查找建议科目：优先费用类借方与负债类（应付）贷方
        List<Account> allAccounts = accountRepository.findAll();
        Account debitAccount = allAccounts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()) && a.getType() == org.example.accounting.domain.AccountType.EXPENSE)
                .findFirst().orElse(null);
        Account creditAccount = allAccounts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActive()) && a.getType() == org.example.accounting.domain.AccountType.LIABILITY)
                .findFirst().orElse(null);

        // 若未找到上述类型，则使用前两个可用账户作为建议（保证分录存在，便于前端修改）
        List<Account> activeAccounts = allAccounts.stream().filter(a -> Boolean.TRUE.equals(a.getActive())).toList();
        if (debitAccount == null || creditAccount == null) {
            if (activeAccounts.size() >= 2) {
                if (debitAccount == null) debitAccount = activeAccounts.get(0);
                if (creditAccount == null) creditAccount = activeAccounts.get(1);
            } else {
                // 无足够账户则仅保存交易主体（不包含分录），管理员需要在前端补充分录
                Transaction savedOnly = transactionRepository.save(txn);
                return transactionMapper.toTransactionResponse(savedOnly);
            }
        }

        Split debitSplit = Split.builder()
                .transaction(txn)
                .account(debitAccount)
                .amount(total)
                .direction(org.example.accounting.domain.DebitCredit.DEBIT)
                .quantity(java.math.BigDecimal.valueOf(purchaseOrder.getQuantity()))
                .price(purchaseOrder.getUnitPrice())
                .memo("来自采购单 " + purchaseOrder.getId())
                .build();

        Split creditSplit = Split.builder()
                .transaction(txn)
                .account(creditAccount)
                .amount(total)
                .direction(org.example.accounting.domain.DebitCredit.CREDIT)
                .memo("来自采购单 " + purchaseOrder.getId())
                .build();

        txn.setSplits(List.of(debitSplit, creditSplit));

        Transaction saved = transactionRepository.save(txn);
        // 通知所有在线用户（前端可监听该事件并弹出或刷新待核对交易列表）
        try {
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("transactionId", saved.getId());
            payload.put("reference", saved.getReference());
            payload.put("description", saved.getDescription());
            notificationService.notifyAllUsers("NEW_DRAFT_TRANSACTION", payload);
        } catch (Exception ignored) { }
        return transactionMapper.toTransactionResponse(saved);
    }

    /**
     * 创建一笔新的双式记账交易。
     * 要求：至少两条分录，且借贷金额总和相等。
     */
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        validationService.validateNewTransaction(request);

        // 获取当前登录用户名
        String createdBy = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getName())) {
            createdBy = authentication.getName();
        }

        Transaction transaction = Transaction.builder()
                .tradeDate(request.getTradeDate())
                .description(request.getDescription())
                .reference(request.getReference())
                .cleared(Boolean.TRUE.equals(request.getCleared()))
                .createdBy(createdBy)
                .build();

        Transaction savedTxn = transactionRepository.save(transaction);

        // 收集所有账户的余额变化（使用Map避免同一账户被重复计算）
        Map<Long, BigDecimal> accountBalanceDeltas = new HashMap<>();

        for (SplitCreateRequest splitReq : request.getSplits()) {
            Long accountId = splitReq.getAccountId();
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new BusinessException("账户不存在: " + accountId));

            // 根据账户类型和借贷方向，计算余额变化
            BigDecimal amount = splitReq.getAmount();
            if (amount == null) {
                throw new BusinessException("分录金额不能为空");
            }
            DebitCredit direction;
            try {
                direction = DebitCredit.valueOf(splitReq.getDirection());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException("非法借贷方向: " + splitReq.getDirection());
            }

            BigDecimal delta;
            if (account.getType() == AccountType.ASSET
                    || account.getType() == AccountType.EXPENSE) {
                // 资产/费用：借方增加，贷方减少
                delta = direction == DebitCredit.DEBIT ? amount : amount.negate();
            } else {
                // 负债/权益/收入：贷方增加，借方减少
                delta = direction == DebitCredit.CREDIT ? amount : amount.negate();
            }

            // 累加同一账户的余额变化（如果同一账户在多条分录中出现）
            BigDecimal previousDelta = accountBalanceDeltas.get(accountId);
            accountBalanceDeltas.merge(accountId, delta, BigDecimal::add);
            BigDecimal newDelta = accountBalanceDeltas.get(accountId);
            
            // 调试日志：如果同一账户出现多次，记录警告
            if (previousDelta != null) {
                System.out.println("警告：账户 " + accountId + " (" + account.getCode() + " - " + account.getName() + ") 在交易中出现了多次！");
                System.out.println("  前一次delta: " + previousDelta);
                System.out.println("  本次delta: " + delta);
                System.out.println("  累计delta: " + newDelta);
            }

            Split split = Split.builder()
                    .transaction(savedTxn)
                    .account(account)
                    .amount(splitReq.getAmount())
                    .direction(direction)
                    .quantity(splitReq.getQuantity())
                    .price(splitReq.getPrice())
                    .memo(splitReq.getMemo())
                    .build();

            if (splitReq.getCommodityId() != null) {
                var commodity = commodityRepository.findById(splitReq.getCommodityId())
                        .orElseThrow(() -> new BusinessException("商品不存在: " + splitReq.getCommodityId()));
                split.setCommodity(commodity);
            }

            splitRepository.save(split);
        }

        // 统一更新所有涉及账户的余额并保存
        // 使用Set确保每个账户只更新一次，避免重复更新
        for (Map.Entry<Long, BigDecimal> entry : accountBalanceDeltas.entrySet()) {
            Long accountId = entry.getKey();
            BigDecimal delta = entry.getValue();
            
            // 重新查询账户，确保获取最新的余额（避免JPA一级缓存问题）
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new BusinessException("账户不存在: " + accountId));
            
            // 计算新余额
            BigDecimal currentBalance = account.getBalance();
            BigDecimal newBalance = currentBalance.add(delta);
            account.setBalance(newBalance);
            
            // 保存账户
            accountRepository.save(account);
        }
        
        // 刷新所有更新，确保立即提交到数据库
        accountRepository.flush();

        // 新交易落账后清理余额缓存
        balanceCalculator.clearCache();

        // 重新加载包含分录的交易
        Transaction full = transactionRepository.findById(savedTxn.getId())
                .orElseThrow(() -> new BusinessException("交易不存在: " + savedTxn.getId()));
        return transactionMapper.toTransactionResponse(full);
    }

    /**
     * 分页查询交易记录。
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> pageTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(transactionMapper::toTransactionResponse);
    }

    /**
     * 分页获取待核对交易（cleared = false）。
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> pageUnclearedTransactions(Pageable pageable) {
        return transactionRepository.findByCleared(false, pageable)
                .map(transactionMapper::toTransactionResponse);
    }

    /**
     * 分页获取已核对/已审批交易（cleared = true）。
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> pageClearedTransactions(Pageable pageable) {
        return transactionRepository.findByCleared(true, pageable)
                .map(transactionMapper::toTransactionResponse);
    }

    /**
     * 支持按关键词搜索已核对交易（在 description 或 reference 中模糊匹配）。
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> searchClearedTransactions(String q, Pageable pageable) {
        return transactionRepository.searchCleared(q, pageable)
                .map(transactionMapper::toTransactionResponse);
    }

    /**
     * 支持按关键词搜索当前用户的已核对交易。
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> searchClearedTransactionsForUser(String username, String q, Pageable pageable) {
        if (username == null) throw new IllegalArgumentException("username required");
        return transactionRepository.searchClearedByUser(username, q, pageable)
                .map(transactionMapper::toTransactionResponse);
    }

    /**
     * 根据ID获取交易实体。
     */
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("交易不存在: " + id));
    }

    /**
     * 根据ID获取交易响应DTO（包含附件信息）。
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionResponse(Long id) {
        Transaction transaction = getTransactionById(id);
        TransactionResponse response = transactionMapper.toTransactionResponse(transaction);
        // 添加附件信息
        if (response != null) {
            List<org.example.accounting.domain.TransactionAttachment> attachments = 
                    attachmentRepository.findByTransactionId(id);
            response.setAttachments(transactionMapper.toAttachmentResponses(attachments));
        }
        return response;
    }

    /**
     * 更新交易：仅允许创建者本人或管理员进行，更新分录并调整相关账户余额。
     */
    @Transactional
    public TransactionResponse updateTransaction(Long id, CreateTransactionRequest request) {
        validationService.validateNewTransaction(request);

        Transaction existing = getTransactionById(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : null;
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (existing.getCreatedBy() != null && !existing.getCreatedBy().equals(currentUser) && !isAdmin) {
            throw new BusinessException("无权限修改该交易");
        }

        // compute balance deltas: reverse old splits then apply new splits
        Map<Long, BigDecimal> accountBalanceDeltas = new HashMap<>();

        // reverse old splits
        for (Split old : existing.getSplits() == null ? java.util.Collections.<Split>emptyList() : existing.getSplits()) {
            Long accountId = old.getAccount().getId();
            Account account = old.getAccount();
            BigDecimal amount = old.getAmount();
            DebitCredit direction = old.getDirection();
            BigDecimal delta;
            if (account.getType() == AccountType.ASSET || account.getType() == AccountType.EXPENSE) {
                delta = direction == DebitCredit.DEBIT ? amount : amount.negate();
            } else {
                delta = direction == DebitCredit.CREDIT ? amount : amount.negate();
            }
            // reversing
            accountBalanceDeltas.merge(accountId, delta.negate(), BigDecimal::add);
            // delete old split
            splitRepository.delete(old);
        }

        // apply new splits
        for (SplitCreateRequest splitReq : request.getSplits()) {
            Long accountId = splitReq.getAccountId();
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new BusinessException("账户不存在: " + accountId));
            BigDecimal amount = splitReq.getAmount();
            if (amount == null) throw new BusinessException("分录金额不能为空");
            DebitCredit direction;
            try { direction = DebitCredit.valueOf(splitReq.getDirection()); }
            catch (IllegalArgumentException ex) { throw new BusinessException("非法借贷方向: " + splitReq.getDirection()); }

            BigDecimal delta;
            if (account.getType() == AccountType.ASSET || account.getType() == AccountType.EXPENSE) {
                delta = direction == DebitCredit.DEBIT ? amount : amount.negate();
            } else {
                delta = direction == DebitCredit.CREDIT ? amount : amount.negate();
            }
            accountBalanceDeltas.merge(accountId, delta, BigDecimal::add);

            Split split = Split.builder()
                    .transaction(existing)
                    .account(account)
                    .amount(splitReq.getAmount())
                    .direction(direction)
                    .quantity(splitReq.getQuantity())
                    .price(splitReq.getPrice())
                    .memo(splitReq.getMemo())
                    .build();
            if (splitReq.getCommodityId() != null) {
                var commodity = commodityRepository.findById(splitReq.getCommodityId())
                        .orElseThrow(() -> new BusinessException("商品不存在: " + splitReq.getCommodityId()));
                split.setCommodity(commodity);
            }
            splitRepository.save(split);
        }

        // apply aggregated deltas to accounts
        for (Map.Entry<Long, BigDecimal> entry : accountBalanceDeltas.entrySet()) {
            Long accountId = entry.getKey();
            BigDecimal delta = entry.getValue();
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new BusinessException("账户不存在: " + accountId));
            account.setBalance(account.getBalance().add(delta));
            accountRepository.save(account);
        }
        accountRepository.flush();

        // update transaction header
        existing.setTradeDate(request.getTradeDate());
        existing.setDescription(request.getDescription());
        existing.setReference(request.getReference());
        existing.setCleared(Boolean.TRUE.equals(request.getCleared()));
        // clear rejection flags on resubmit
        existing.setRejected(false);
        existing.setRejectionReason(null);
        existing.setRejectedAt(null);
        existing.setRejectedBy(null);

        Transaction saved = transactionRepository.save(existing);

        // clear balance cache
        balanceCalculator.clearCache();

        // notify admin that a resubmission occurred
        try {
            notificationService.notifyUser("boss", "transaction_resubmitted",
                    java.util.Map.of("transactionId", saved.getId(), "submittedBy", currentUser, "submittedAt", java.time.LocalDateTime.now()));
            // also broadcast to all connected users as fallback to ensure boss receives
            notificationService.notifyAllUsers("transaction_resubmitted",
                    java.util.Map.of("transactionId", saved.getId(), "submittedBy", currentUser, "submittedAt", java.time.LocalDateTime.now()));
        } catch (Exception ignored) {}

        return transactionMapper.toTransactionResponse(saved);
    }

    /**
     * 审核交易。
     */
    @Transactional
    public TransactionResponse approveTransaction(Long id, Boolean approved) {
        Transaction transaction = getTransactionById(id);
        if (transaction.getCleared() != null && transaction.getCleared()) {
            throw new BusinessException("该交易已审核，不能重复审核");
        }
        transaction.setCleared(approved);
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponse(saved);
    }

    /**
     * Overload: approve or reject with optional reason (if reject).
     */
    @Transactional
    public TransactionResponse approveTransaction(Long id, Boolean approved, String reason) {
        Transaction transaction = getTransactionById(id);
        if (transaction.getCleared() != null && transaction.getCleared()) {
            throw new BusinessException("该交易已审核，不能重复审核");
        }
        if (Boolean.TRUE.equals(approved)) {
            transaction.setCleared(true);
            transaction.setRejected(false);
            transaction.setRejectionReason(null);
            transaction.setRejectedAt(null);
            transaction.setRejectedBy(null);
        } else {
            // require non-empty rejection reason
            if (reason == null || reason.trim().isEmpty()) {
                throw new BusinessException("驳回必须提供理由");
            }
            transaction.setCleared(false);
            transaction.setRejected(true);
            transaction.setRejectionReason(reason.trim());
            transaction.setRejectedAt(java.time.LocalDateTime.now());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            transaction.setRejectedBy(auth != null ? auth.getName() : null);
        }
        Transaction saved = transactionRepository.save(transaction);
        // notify the owner (createdBy) if rejected
        if (Boolean.FALSE.equals(approved) && saved.getCreatedBy() != null) {
            try {
                var payload = java.util.Map.of(
                        "transactionId", saved.getId(),
                        "reason", saved.getRejectionReason(),
                        "rejectedBy", saved.getRejectedBy(),
                        "rejectedAt", saved.getRejectedAt()
                );
                notificationService.notifyUser(saved.getCreatedBy(), "transaction_rejected", payload);
            } catch (Exception ignored) {}
        }
        return transactionMapper.toTransactionResponse(saved);
    }

    /**
     * 统计当前登录用户被驳回且未核对的交易数量（用于员工端红点提醒）。
     */
    @Transactional(readOnly = true)
    public long countRejectedForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return 0;
        String username = auth.getName();
        return transactionRepository.countByCreatedByAndRejectedTrueAndClearedFalse(username);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<TransactionResponse> pageRejectedForCurrentUser(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return org.springframework.data.domain.Page.empty();
        String username = auth.getName();
        Page<Transaction> page = transactionRepository.findByCreatedByAndRejectedTrueAndClearedFalse(username, pageable);
        List<TransactionResponse> dtos = transactionMapper.toTransactionResponses(page.getContent());
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, page.getTotalElements());
    }
}


