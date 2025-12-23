package org.example.accounting.repository;

import java.util.List;
import java.util.Optional;
import org.example.accounting.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 账户仓储接口
 * 使用 Spring Data JPA 提供基本 CRUD 能力。
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * 根据父账户查询子账户列表。
     *
     * @param parentId 父账户 ID，可为空（查询根账户）
     * @return 子账户列表
     */
    List<Account> findByParentId(Long parentId);

    /**
     * 根据编码查找账户。
     *
     * @param code 科目代码
     * @return 账户
     */
    Optional<Account> findByCode(String code);
}












