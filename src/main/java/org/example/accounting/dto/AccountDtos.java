package org.example.accounting.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 账户相关 DTO 定义。
 */
public class AccountDtos {

    /**
     * 创建账户请求 DTO。
     */
    @Data
    public static class CreateAccountRequest {

        /**
         * 账户名称
         */
        private String name;

        /**
         * 科目代码
         */
        private String code;

        /**
         * 账户类型：ASSET/LIABILITY/EQUITY/INCOME/EXPENSE
         */
        private String type;

        /**
         * 货币符号，例如：CNY
         */
        private String currencySymbol;
    }

    /**
     * 调整账户余额请求 DTO。
     */
    @Data
    public static class AdjustBalanceRequest {

        /**
         * 新的余额数值
         */
        private BigDecimal balance;
    }

    /**
     * 账户返回 DTO。
     */
    @Data
    public static class AccountResponse {

        private Long id;

        private String name;

        private String code;

        private String type;

        private BigDecimal balance;

        private String currencySymbol;

        private Long parentId;
    }

    // 已移除账户树相关 DTO（AccountTreeNode），如需树形结构可在前端基于 AccountResponse 自行组装。
}


