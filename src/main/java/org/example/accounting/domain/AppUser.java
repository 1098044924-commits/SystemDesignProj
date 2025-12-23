package org.example.accounting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 系统用户实体，用于 Spring Security 认证。
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 登录用户名（唯一）。
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 登录密码（BCrypt 加密后存储）。
     */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /**
     * 真实姓名。
     */
    @Column(name = "real_name", nullable = false, length = 100)
    private String realName;

    /**
     * 用户角色，简单起见只分 USER / ADMIN。
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    /**
     * 是否启用。
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    /**
     * 密码最后更新时间。
     */
    @Column(name = "password_changed_at")
    private java.time.LocalDateTime passwordChangedAt;
}



