package org.example.accounting.mapper;

import java.util.ArrayList;
import java.util.List;
import org.example.accounting.domain.Split;
import org.example.accounting.domain.Transaction;
import org.example.accounting.domain.TransactionAttachment;
import org.example.accounting.dto.TransactionDtos.AttachmentResponse;
import org.example.accounting.dto.TransactionDtos.SplitResponse;
import org.example.accounting.dto.TransactionDtos.TransactionResponse;
import org.mapstruct.Mapper;

/**
 * 交易与分录相关 MapStruct 映射接口。
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * 分录实体 -> 分录返回 DTO。
     * 使用默认方法手动映射，避免 MapStruct 对 Lombok 属性分析带来的干扰。
     */
    default SplitResponse toSplitResponse(Split split) {
        if (split == null) {
            return null;
        }
        SplitResponse dto = new SplitResponse();
        dto.setId(split.getId());
        if (split.getAccount() != null) {
            dto.setAccountId(split.getAccount().getId());
            dto.setAccountName(split.getAccount().getName());
            dto.setAccountCode(split.getAccount().getCode());
        }
        dto.setAmount(split.getAmount());
        dto.setDirection(split.getDirection() != null ? split.getDirection().name() : null);
        dto.setQuantity(split.getQuantity());
        dto.setPrice(split.getPrice());
        if (split.getCommodity() != null) {
            dto.setCommodityId(split.getCommodity().getId());
            dto.setCommoditySymbol(split.getCommodity().getSymbol());
        }
        dto.setMemo(split.getMemo());
        return dto;
    }

    /**
     * 交易实体 -> 交易返回 DTO。
     */
    default TransactionResponse toTransactionResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        TransactionResponse dto = new TransactionResponse();
        dto.setId(transaction.getId());
        dto.setTradeDate(transaction.getTradeDate());
        dto.setDescription(transaction.getDescription());
        dto.setReference(transaction.getReference());
        dto.setCleared(transaction.getCleared());
        dto.setOperator(transaction.getCreatedBy());
        dto.setSplits(toSplitResponses(transaction.getSplits()));
        // map rejection/audit metadata so frontend can show rejection reason and status
        try {
            dto.setRejectionReason(transaction.getRejectionReason());
            dto.setRejected(transaction.getRejected());
            dto.setRejectedAt(transaction.getRejectedAt());
            dto.setRejectedBy(transaction.getRejectedBy());
        } catch (Exception ignored) {}
        return dto;
    }

    /**
     * 批量转换分录。
     */
    default List<SplitResponse> toSplitResponses(List<Split> splits) {
        if (splits == null) {
            return List.of();
        }
        List<SplitResponse> result = new ArrayList<>(splits.size());
        for (Split split : splits) {
            result.add(toSplitResponse(split));
        }
        return result;
    }

    /**
     * 批量转换交易。
     */
    default List<TransactionResponse> toTransactionResponses(List<Transaction> transactions) {
        if (transactions == null) {
            return List.of();
        }
        List<TransactionResponse> result = new ArrayList<>(transactions.size());
        for (Transaction tx : transactions) {
            result.add(toTransactionResponse(tx));
        }
        return result;
    }

    /**
     * 附件实体 -> 附件响应 DTO。
     */
    default AttachmentResponse toAttachmentResponse(TransactionAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        AttachmentResponse dto = new AttachmentResponse();
        dto.setId(attachment.getId());
        dto.setOriginalFilename(attachment.getOriginalFilename());
        dto.setContentType(attachment.getContentType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedAt(attachment.getUploadedAt());
        return dto;
    }

    /**
     * 批量转换附件。
     */
    default List<AttachmentResponse> toAttachmentResponses(List<TransactionAttachment> attachments) {
        if (attachments == null) {
            return List.of();
        }
        List<AttachmentResponse> result = new ArrayList<>(attachments.size());
        for (TransactionAttachment attachment : attachments) {
            result.add(toAttachmentResponse(attachment));
        }
        return result;
    }
}


