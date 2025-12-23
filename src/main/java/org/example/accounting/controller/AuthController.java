package org.example.accounting.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.example.accounting.domain.AppUser;
import org.example.accounting.domain.UserRole;
import org.example.accounting.dto.AuthDtos.ForgotPasswordRequest;
import org.example.accounting.dto.AuthDtos.RegisterRequest;
import org.example.accounting.dto.AuthDtos.UserInfoResponse;
import org.example.accounting.dto.AuthDtos.UserListResponse;
import org.example.accounting.exception.BusinessException;
import org.example.accounting.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口（注册和用户信息）。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 检查用户名是否已存在。
     */
    @GetMapping("/check-username")
    public java.util.Map<String, Object> checkUsername(@org.springframework.web.bind.annotation.RequestParam String username) {
        boolean exists = userRepository.existsByUsername(username);
        // 禁止使用管理员账户名
        boolean isBoss = "boss".equalsIgnoreCase(username);
        return java.util.Map.of(
            "exists", exists || isBoss,
            "available", !exists && !isBoss
        );
    }

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/me")
    public UserInfoResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("未登录");
        }
        
        String username = authentication.getName();
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return UserInfoResponse.builder()
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole().name())
                .isAdmin(user.getRole() == UserRole.ROLE_ADMIN)
                .build();
    }

    /**
     * 用户注册：创建一个 ROLE_USER 账号（会计账户）。
     * 管理员账户（boss）不能通过注册创建。
     */
    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (request.getRealName() == null || request.getRealName().isBlank()) {
            throw new BusinessException("真实姓名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("密码长度至少 6 位");
        }
        // 禁止注册管理员账户
        if ("boss".equalsIgnoreCase(request.getUsername())) {
            throw new BusinessException("管理员账户不能注册");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .realName(request.getRealName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_USER)  // 注册的用户都是会计角色
                .enabled(true)
                .build();

        userRepository.save(user);
    }

    /**
     * 获取所有员工账户列表（仅管理员可访问）。
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserListResponse> listUsers() {
        List<AppUser> users = userRepository.findAll();
        return users.stream()
                .map(user -> UserListResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .role(user.getRole().name())
                        .enabled(user.getEnabled())
                        .passwordChangedAt(user.getPasswordChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 删除员工账户（仅管理员可访问，不能删除管理员账户）。
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 不能删除管理员账户
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new BusinessException("不能删除管理员账户");
        }
        
        userRepository.delete(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 忘记密码：通过用户名和真实姓名重置密码。
     */
    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (request.getRealName() == null || request.getRealName().isBlank()) {
            throw new BusinessException("真实姓名不能为空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度至少 6 位");
        }

        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或真实姓名不正确"));

        // 验证真实姓名
        if (!user.getRealName().equals(request.getRealName())) {
            throw new BusinessException("用户名或真实姓名不正确");
        }

        // 更新密码并记录更新时间
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }
}



