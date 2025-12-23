package org.example.accounting.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Spring Security 配置。
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 简化起见，先关闭 CSRF，方便表单和前端调试
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 静态资源和登录/注册接口放行
                        .requestMatchers(
                                "/login.html",
                                "/register.html",
                                "/index.html",
                                "/accountant.html",
                                "/",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/api/auth/register",
                                "/api/auth/check-username",
                                "/api/auth/forgot-password"
                        ).permitAll()
                        // allow anonymous SSE subscribe endpoint so EventSource preflight and connections succeed
                        .requestMatchers(
                                "/api/notifications/subscribe",
                                "/api/notifications/**"
                        ).permitAll()
                        // 账户列表查询允许所有已认证用户访问（会计需要选择账户）
                        .requestMatchers(
                                "/api/accounts"
                        ).authenticated()
                        // 账户创建、修改、删除仅管理员可访问
                        .requestMatchers(
                                "/api/accounts/**",
                                "/api/commodities",
                                "/api/commodities/**",
                                "/api/auth/users",
                                "/api/auth/users/**"
                        ).hasRole("ADMIN")
                        // 交易、报表、对账接口所有已认证用户可访问（管理员和会计）
                        .requestMatchers(
                                "/api/transactions",
                                "/api/transactions/**",
                                "/api/reports",
                                "/api/reports/**",
                                "/api/auth/me"
                        ).authenticated()
                        // 其他所有接口需要认证
                        .anyRequest().authenticated()
                )
                // 表单登录配置，使用我们自定义的 login.html 页面
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/login")
                        .successHandler(new AuthenticationSuccessHandler() {
                            @Override
                            public void onAuthenticationSuccess(HttpServletRequest request,
                                    HttpServletResponse response, Authentication authentication) throws IOException {
                                // 根据用户角色跳转到不同页面
                                boolean isAdmin = authentication.getAuthorities().contains(
                                        new SimpleGrantedAuthority("ROLE_ADMIN"));
                                if (isAdmin) {
                                    response.sendRedirect("/index.html");
                                } else {
                                    response.sendRedirect("/accountant.html");
                                }
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}


