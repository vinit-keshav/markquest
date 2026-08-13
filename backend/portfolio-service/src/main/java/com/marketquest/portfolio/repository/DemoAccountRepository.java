package com.marketquest.portfolio.repository;

import com.marketquest.portfolio.entity.DemoAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoAccountRepository extends JpaRepository<DemoAccount, Long> {
    List<DemoAccount> findByUserIdOrderByCurrency(String userId);

    Optional<DemoAccount> findByUserIdAndCurrency(String userId, String currency);
}
