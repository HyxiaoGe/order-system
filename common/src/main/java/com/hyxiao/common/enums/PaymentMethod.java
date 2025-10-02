package com.hyxiao.common.enums;

/**
 * 支付方式枚举
 */
public enum PaymentMethod {
    
    /**
     * 支付宝
     */
    ALIPAY("支付宝"),
    
    /**
     * 微信支付
     */
    WECHAT_PAY("微信支付"),
    
    /**
     * 银行卡
     */
    BANK_CARD("银行卡"),
    
    /**
     * 信用卡
     */
    CREDIT_CARD("信用卡"),
    
    /**
     * 余额支付
     */
    BALANCE("余额支付");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}