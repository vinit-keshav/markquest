package com.marketquest.watchlist_service.repository;

import com.marketquest.watchlist_service.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    
}