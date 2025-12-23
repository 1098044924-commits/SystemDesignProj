package org.example.accounting.service;

import java.math.BigDecimal;
import org.example.accounting.domain.Account;
import org.example.accounting.domain.AccountType;
import org.example.accounting.dto.AccountDtos.AccountResponse;
import org.example.accounting.dto.AccountDtos.AdjustBalanceRequest;
import org.example.accounting.dto.AccountDtos.CreateAccountRequest;
import org.example.accounting.exception.BusinessException;
import org.example.accounting.mapper.AccountMapper;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.repository.CommodityRepository;
import org.example.accounting.repository.SplitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账户服务层
 * 负责账户创建及余额调整等核心逻辑（已移除账户树功能）。
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CommodityRepository commodityRepository;
    private final AccountMapper accountMapper;
    private final SplitRepository splitRepository;

    public AccountService(AccountRepository accountRepository,
            CommodityRepository commodityRepository,
            AccountMapper accountMapper,
            SplitRepository splitRepository) {
        this.accountRepository = accountRepository;
        this.commodityRepository = commodityRepository;
        this.accountMapper = accountMapper;
        this.splitRepository = splitRepository;
    }

    /**
     * 查询所有账户列表（扁平结构）。
     */
    @Transactional(readOnly = true)
    public java.util.List<AccountResponse> listAccounts() {
        return accountMapper.toAccountResponses(accountRepository.findAll());
    }

    /**
     * 创建新账户。
     *
     * @param request 创建请求
     * @return 创建后的账户信息
     */
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        AccountType type;
        try {
            type = AccountType.valueOf(request.getType());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("非法账户类型: " + request.getType());
        }

        var currency = commodityRepository.findBySymbol(request.getCurrencySymbol())
                .orElseThrow(() -> new BusinessException("货币不存在: " + request.getCurrencySymbol()));

        Account account = Account.builder()
                .name(request.getName())
                .code(request.getCode())
                .type(type)
                .balance(BigDecimal.ZERO)
                .active(true)  // 新创建的账户默认为激活状态
                .currency(currency)
                .parent(null)
                .build();

        Account saved = accountRepository.save(account);
        return accountMapper.toAccountResponse(saved);
    }

    /**
     * 调整账户余额（直接设置为新值）。
     *
     * @param id      账户 ID
     * @param request 请求体，包含新余额
     * @return 更新后的账户信息
     */
    @Transactional
    public AccountResponse adjustBalance(Long id, AdjustBalanceRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("账户不存在: " + id));
        account.setBalance(request.getBalance());
        Account saved = accountRepository.save(account);
        return accountMapper.toAccountResponse(saved);
    }

    /**
     * 删除账户。
     * 删除前会检查：
     * 1. 账户是否存在
     * 2. 是否有子账户（如果有，不允许删除）
     * 3. 是否有交易记录（如果有，不允许删除）
     *
     * @param id 账户 ID
     */
    @Transactional
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("账户不存在: " + id));

        // 检查是否有子账户
        var children = accountRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("账户存在子账户，无法删除。请先删除或移动子账户。");
        }

        // 检查是否有交易记录
        if (splitRepository.existsByAccountId(id)) {
            throw new BusinessException("账户存在交易记录，无法删除。如需停用账户，请将账户设置为非激活状态。");
        }

        // 执行删除
        accountRepository.delete(account);
    }
}


