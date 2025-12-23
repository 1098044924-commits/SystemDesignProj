package org.example.accounting.config;

import org.example.accounting.domain.AppUser;
import org.example.accounting.domain.Commodity;
import org.example.accounting.domain.CommodityType;
import org.example.accounting.domain.UserRole;
import org.example.accounting.repository.AppUserRepository;
import org.example.accounting.repository.AccountRepository;
import org.example.accounting.domain.Account;
import org.example.accounting.domain.AccountType;
import java.math.BigDecimal;
import org.example.accounting.repository.CommodityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时自动创建常用货币数据和管理员账户。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CommodityRepository commodityRepository;
    private final AppUserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CommodityRepository commodityRepository,
                          AppUserRepository userRepository,
                          AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder) {
        this.commodityRepository = commodityRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("开始初始化数据...");
        // 初始化常用货币
        initCurrencies();
        // 初始化管理员和员工账户
        initAdminUser();
        initEmployeeUser();
        // 初始化默认账户（现金等），以保证空数据库也能运行
        initDefaultAccounts();
        log.info("数据初始化完成");
    }

    private void initCurrencies() {
        // 亚洲常用货币（10个）
        // 1. 人民币（中国）
        createCurrencyIfNotExists("CNY", "人民币", 2);
        // 2. 日元（日本）
        createCurrencyIfNotExists("JPY", "日元", 0);
        // 3. 韩元（韩国）
        createCurrencyIfNotExists("KRW", "韩元", 0);
        // 4. 港币（香港）
        createCurrencyIfNotExists("HKD", "港币", 2);
        // 5. 新台币（台湾）
        createCurrencyIfNotExists("TWD", "新台币", 2);
        // 6. 新加坡元（新加坡）
        createCurrencyIfNotExists("SGD", "新加坡元", 2);
        // 7. 马来西亚林吉特（马来西亚）
        createCurrencyIfNotExists("MYR", "马来西亚林吉特", 2);
        // 8. 泰铢（泰国）
        createCurrencyIfNotExists("THB", "泰铢", 2);
        // 9. 印度卢比（印度）
        createCurrencyIfNotExists("INR", "印度卢比", 2);
        // 10. 印尼盾（印尼）
        createCurrencyIfNotExists("IDR", "印尼盾", 0);
    }

    private void createCurrencyIfNotExists(String symbol, String name, int fraction) {
        if (!commodityRepository.findBySymbol(symbol).isPresent()) {
            Commodity commodity = Commodity.builder()
                    .symbol(symbol)
                    .name(name)
                    .type(CommodityType.CURRENCY)
                    .fraction(fraction)
                    .build();
            commodityRepository.save(commodity);
            log.info("已创建货币: {} - {}", symbol, name);
        } else {
            log.debug("货币已存在，跳过: {} - {}", symbol, name);
        }
    }

    /**
     * 初始化管理员账户：boss / admin123
     */
    private void initAdminUser() {
        String adminUsername = "boss";
        if (!userRepository.existsByUsername(adminUsername)) {
            AppUser admin = AppUser.builder()
                    .username(adminUsername)
                    .realName("管理员")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ROLE_ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("已创建管理员账户: {} / admin123", adminUsername);
        } else {
            log.debug("管理员账户已存在，跳过");
        }
    }

    /**
     * 初始化员工账户：acct001 / admin123
     */
    private void initEmployeeUser() {
        String userName = "acct001";
        if (!userRepository.existsByUsername(userName)) {
            AppUser user = AppUser.builder()
                    .username(userName)
                    .realName("会计用户")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ROLE_USER)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("已创建员工账户: {} / admin123", userName);
        } else {
            log.debug("员工账户已存在，跳过");
        }
    }

    /**
     * 初始化默认账户（最少一个资产类账户），以避免系统在无账户时部分功能失败。
     */
    private void initDefaultAccounts() {
        try {
            // 使用人民币作为默认货币
            var opt = commodityRepository.findBySymbol("CNY");
            if (opt.isPresent()) {
                var cny = opt.get();
                // 现金账户 code: 1000
                if (!accountRepository.findByCode("1000").isPresent()) {
                    Account cash = Account.builder()
                            .name("现金")
                            .code("1000")
                            .type(AccountType.ASSET)
                            .balance(BigDecimal.ZERO)
                            .active(true)
                            .currency(cny)
                            .parent(null)
                            .build();
                    accountRepository.save(cash);
                    log.info("已创建默认账户: 现金 (1000)");
                } else {
                    log.debug("默认现金账户已存在，跳过");
                }
            } else {
                log.warn("未找到默认货币 CNY，跳过默认账户创建");
            }
        } catch (Exception e) {
            log.error("创建默认账户时出错", e);
        }
    }
}

