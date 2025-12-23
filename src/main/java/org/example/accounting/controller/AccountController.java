package org.example.accounting.controller;

import java.util.List;
import org.example.accounting.dto.AccountDtos.AccountResponse;
import org.example.accounting.dto.AccountDtos.AdjustBalanceRequest;
import org.example.accounting.dto.AccountDtos.CreateAccountRequest;
import org.example.accounting.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账户管理 REST 控制器（不再提供账户树功能）。
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 获取所有账户列表（扁平结构），用于前端下拉选择。
     * 允许所有已认证用户访问（管理员和会计都需要选择账户）。
     */
    @GetMapping
    public List<AccountResponse> list() {
        return accountService.listAccounts();
    }

    /**
     * 创建账户。
     */
    @PostMapping
    public AccountResponse create(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    /**
     * 调整账户余额。
     */
    @PutMapping("/{id}/balance")
    public AccountResponse adjustBalance(@PathVariable("id") Long id,
            @RequestBody AdjustBalanceRequest request) {
        return accountService.adjustBalance(id, request);
    }

    /**
     * 删除账户。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}


