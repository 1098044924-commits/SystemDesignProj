package org.example.accounting.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 认证/注册相关 DTO。
 */
public class AuthDtos {

    /**
     * 用户注册请求。
     */
    @Data
    public static class RegisterRequest {
        private String username;
        private String realName;
        private String password;
    }

    /**
     * 当前用户信息响应。
     */
    @Data
    @Builder
    public static class UserInfoResponse {
        private String username;
        private String realName;
        private String role;
        private Boolean isAdmin;
    }

    /**
     * 用户列表响应（用于管理员查看员工账户）。
     */
    @Data
    @Builder
    public static class UserListResponse {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private Boolean enabled;
        private java.time.LocalDateTime passwordChangedAt;
    }

    /**
     * 忘记密码请求。
     */
    @Data
    public static class ForgotPasswordRequest {
        private String username;
        private String realName;
        private String newPassword;
    }
}



