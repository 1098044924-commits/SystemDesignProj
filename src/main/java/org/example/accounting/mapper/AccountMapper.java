package org.example.accounting.mapper;

import java.util.ArrayList;
import java.util.List;
import org.example.accounting.domain.Account;
import org.example.accounting.dto.AccountDtos.AccountResponse;
import org.mapstruct.Mapper;

/**
 * 账户相关 MapStruct 映射接口（已移除账户树映射）。
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Entity -> 普通账户返回 DTO。
     */
    default AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }
        AccountResponse dto = new AccountResponse();
        dto.setId(account.getId());
        dto.setName(account.getName());
        dto.setCode(account.getCode());
        dto.setType(account.getType() != null ? account.getType().name() : null);
        dto.setBalance(account.getBalance());
        if (account.getCurrency() != null) {
            dto.setCurrencySymbol(account.getCurrency().getSymbol());
        }
        dto.setParentId(account.getParent() != null ? account.getParent().getId() : null);
        return dto;
    }

    /**
     * 批量转换。
     */
    default List<AccountResponse> toAccountResponses(List<Account> accounts) {
        if (accounts == null) {
            return List.of();
        }
        List<AccountResponse> result = new ArrayList<>(accounts.size());
        for (Account account : accounts) {
            result.add(toAccountResponse(account));
        }
        return result;
    }
}

