package org.example.accounting.exception;

/**
 * 业务异常
 * 用于在服务层抛出可预期的业务错误，例如借贷不平衡、账户不存在等。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}












