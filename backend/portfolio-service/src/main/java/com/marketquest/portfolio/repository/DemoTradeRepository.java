package com.marketquest.portfolio.repository;

import com.marketquest.portfolio.entity.DemoTrade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoTradeRepository extends JpaRepository<DemoTrade, Long> {
    boolean existsByTradeId(String tradeId);

    List<DemoTrade> findTop20ByUserIdOrderByExecutedAtDesc(String userId);
}
