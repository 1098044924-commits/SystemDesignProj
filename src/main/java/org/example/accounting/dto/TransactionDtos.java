package org.example.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 交易及分录相关 DTO 定义。
 */
public class TransactionDtos {

    /**
     * 分录创建请求 DTO。
     */
    @Data
    public static class SplitCreateRequest {

        /**
         * 账户 ID
         */
        private Long accountId;

        /**
         * 金额
         */
        private BigDecimal amount;

        /**
         * 借贷方向：DEBIT/CREDIT
         */
        private String direction;

        /**
         * 数量（可选）
         */
        private BigDecimal quantity;

        /**
         * 单价（可选）
         */
        private BigDecimal price;

        /**
         * 商品 ID（可选）
         */
        private Long commodityId;

        /**
         * 备注
         */
        private String memo;
    }

    /**
     * 创建交易请求 DTO。
     */
    @Data
    public static class CreateTransactionRequest {

        /**
         * 交易日期
         */
        private LocalDateTime tradeDate;

        /**
         * 描述
         */
        private String description;

        /**
         * 参考号
         */
        private String reference;

        /**
         * 是否已核对
         */
        private Boolean cleared;

        /**
         * 分录列表
         */
        private List<SplitCreateRequest> splits;
    }

    /**
     * 分录返回 DTO。
     */
    @Data
    public static class SplitResponse {

        private Long id;
        private Long accountId;
        private String accountName;
        private String accountCode;
        private BigDecimal amount;
        private String direction;
        private BigDecimal quantity;
        private BigDecimal price;
        private Long commodityId;
        private String commoditySymbol;
        private String memo;
    }

    /**
     * 附件响应 DTO。
     */
    @Data
    public static class AttachmentResponse {
        private Long id;
        private String originalFilename;
        private String contentType;
        private Long fileSize;
        private java.time.LocalDateTime uploadedAt;
    }

    /**
     * 交易返回 DTO。
     */
    @Data
    public static class TransactionResponse {

        private Long id;
        private LocalDateTime tradeDate;
        private String description;
        private String reference;
        private Boolean cleared;
        private String operator;  // 操作员（创建该交易的用户名）
        private List<SplitResponse> splits;
        private List<AttachmentResponse> attachments;  // 附件列表
        // 驳回/审核相关字段
        private Boolean rejected;
        private String rejectionReason;
        private LocalDateTime rejectedAt;
        private String rejectedBy;
    }
}



