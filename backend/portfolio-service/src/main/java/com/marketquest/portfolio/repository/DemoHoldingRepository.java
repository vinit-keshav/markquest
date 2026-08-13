package com.marketquest.portfolio.repository;

import com.marketquest.portfolio.entity.DemoHolding;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoHoldingRepository extends JpaRepository<DemoHolding, Long> {
    List<DemoHolding> findByUserIdOrderBySymbol(String userId);

    Optional<DemoHolding> findByUserIdAndSymbol(String userId, String symbol);
}
