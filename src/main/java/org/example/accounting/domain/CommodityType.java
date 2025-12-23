package org.example.accounting.domain;

/**
 * 商品/服务/货币类型
 * 用于区分货币、股票、基金以及一般商品或服务。
 */
public enum CommodityType {

    /**
     * 货币（如 CNY、USD）
     */
    CURRENCY,

    /**
     * 股票
     */
    STOCK,

    /**
     * 基金或类似证券
     */
    FUND,

    /**
     * 一般商品或服务
     */
    GOOD_OR_SERVICE
}












