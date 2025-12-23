package org.example.accounting.dto;

import lombok.Data;

/**
 * 货币/商品相关 DTO 定义。
 */
public class CommodityDtos {

    /**
     * 创建货币请求 DTO。
     */
    @Data
    public static class CreateCommodityRequest {
        /**
         * 符号/代码，例如：CNY、USD
         */
        private String symbol;

        /**
         * 名称，例如：人民币、美元
         */
        private String name;

        /**
         * 类型：CURRENCY/STOCK/FUND/GOOD_OR_SERVICE
         */
        private String type;

        /**
         * 小数位数
         */
        private Integer fraction;
    }

    /**
     * 货币返回 DTO。
     */
    @Data
    public static class CommodityResponse {
        private Long id;
        private String symbol;
        private String name;
        private String type;
        private Integer fraction;
    }
}











