package org.example.accounting.repository;

import java.util.Optional;
import org.example.accounting.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 系统用户仓储。
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}



