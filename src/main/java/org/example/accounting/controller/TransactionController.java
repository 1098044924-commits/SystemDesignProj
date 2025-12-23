package org.example.accounting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.accounting.domain.TransactionAttachment;
import org.example.accounting.dto.TransactionDtos.CreateTransactionRequest;
import org.example.accounting.dto.TransactionDtos.TransactionResponse;
import org.example.accounting.repository.TransactionAttachmentRepository;
import org.example.accounting.service.FileStorageService;
import org.example.accounting.service.TransactionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * 交易管理 REST 控制器。
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final FileStorageService fileStorageService;
    private final TransactionAttachmentRepository attachmentRepository;
    private final ObjectMapper objectMapper;

    public TransactionController(TransactionService transactionService,
                                FileStorageService fileStorageService,
                                TransactionAttachmentRepository attachmentRepository,
                                ObjectMapper objectMapper) {
        this.transactionService = transactionService;
        this.fileStorageService = fileStorageService;
        this.attachmentRepository = attachmentRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建双式记账交易（支持文件上传）。
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TransactionResponse createWithFiles(
            @RequestPart("transaction") String transactionJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        try {
            CreateTransactionRequest request = objectMapper.readValue(transactionJson, CreateTransactionRequest.class);
            TransactionResponse response = transactionService.createTransaction(request);
            
            // 如果有文件，保存附件
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String storedFilename = fileStorageService.storeFile(file);
                        TransactionAttachment attachment = TransactionAttachment.builder()
                                .transaction(transactionService.getTransactionById(response.getId()))
                                .originalFilename(file.getOriginalFilename())
                                .storedFilename(storedFilename)
                                .contentType(file.getContentType())
                                .fileSize(file.getSize())
                                .uploadedAt(LocalDateTime.now())
                                .build();
                        attachmentRepository.save(attachment);
                    }
                }
            }
            
            return response;
        } catch (IOException e) {
            throw new RuntimeException("解析交易数据失败", e);
        }
    }

    /**
     * 创建双式记账交易（JSON格式，兼容旧接口）。
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public TransactionResponse create(@RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    /**
     * 下载交易附件。
     */
    @GetMapping("/{transactionId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long transactionId,
            @PathVariable Long attachmentId) {
        TransactionAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("附件不存在"));
        
        if (!attachment.getTransaction().getId().equals(transactionId)) {
            throw new RuntimeException("附件不属于该交易");
        }
        
        try {
            Path filePath = fileStorageService.getFilePath(attachment.getStoredFilename());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("文件不存在或无法读取");
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType() != null ? attachment.getContentType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("下载文件失败", e);
        }
    }

    /**
     * 分页查询交易记录。
     */
    @GetMapping
    public Page<TransactionResponse> page(Pageable pageable) {
        return transactionService.pageTransactions(pageable);
    }

    /**
     * 获取待核对交易（未清算）。
     */
    @GetMapping("/reconcile")
    public Page<TransactionResponse> reconcile(Pageable pageable) {
        return transactionService.pageUnclearedTransactions(pageable);
    }

    /**
     * 管理员查看已核对/历史交易记录（分页）。
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TransactionResponse> history(@RequestParam(name = "search", required = false) String search, Pageable pageable) {
        return transactionService.searchClearedTransactions(search, pageable);
    }

    /**
     * 当前登录用户查看自己的已核对交易（分页 + 可搜索）。
     */
    @GetMapping("/history/mine")
    public Page<TransactionResponse> historyMine(@RequestParam(name = "search", required = false) String search, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        return transactionService.searchClearedTransactionsForUser(username, search, pageable);
    }

    /**
     * 获取交易详情（包括附件信息）。
     */
    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable Long id) {
        return transactionService.getTransactionResponse(id);
    }

    /**
     * 更新交易（JSON格式，兼容旧接口）。
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TransactionResponse update(@PathVariable Long id, @RequestBody CreateTransactionRequest request) {
        return transactionService.updateTransaction(id, request);
    }

    /**
     * 更新交易并可上传附件（multipart/form-data）。
     */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TransactionResponse updateWithFiles(
            @PathVariable Long id,
            @RequestPart("transaction") String transactionJson,
            @RequestPart(value = "files", required = false) MultipartFile[] files) {
        try {
            CreateTransactionRequest request = objectMapper.readValue(transactionJson, CreateTransactionRequest.class);
            TransactionResponse response = transactionService.updateTransaction(id, request);
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String storedFilename = fileStorageService.storeFile(file);
                        TransactionAttachment attachment = TransactionAttachment.builder()
                                .transaction(transactionService.getTransactionById(response.getId()))
                                .originalFilename(file.getOriginalFilename())
                                .storedFilename(storedFilename)
                                .contentType(file.getContentType())
                                .fileSize(file.getSize())
                                .uploadedAt(LocalDateTime.now())
                                .build();
                        attachmentRepository.save(attachment);
                    }
                }
            }
            return response;
        } catch (IOException e) {
            throw new RuntimeException("解析交易数据失败", e);
        }
    }

    /**
     * 获取当前登录用户的被驳回数量（用于员工侧红点提醒）。
     */
    @GetMapping("/rejected/count")
    public java.util.Map<String, Object> rejectedCountForCurrentUser() {
        long c = transactionService.countRejectedForCurrentUser();
        return java.util.Collections.singletonMap("count", c);
    }

    /**
     * 当前登录用户查看自己被驳回且未核对的交易（用于员工查看驳回原因）。
     */
    @GetMapping("/rejected/mine")
    public org.springframework.data.domain.Page<TransactionResponse> rejectedForCurrentUser(org.springframework.data.domain.Pageable pageable) {
        return transactionService.pageRejectedForCurrentUser(pageable);
    }

    /**
     * 审核交易（通过或拒绝）。
     */
    @PutMapping("/{id}/approve")
    public TransactionResponse approveTransaction(@PathVariable Long id,
                                                  @RequestParam Boolean approved,
                                                  @RequestParam(name = "reason", required = false) String reason) {
        // forward to service; service has overload to accept optional reason
        return transactionService.approveTransaction(id, approved, reason);
    }
}



