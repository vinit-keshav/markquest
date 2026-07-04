package com.marketquest.watchlist_service.service;

import com.marketquest.watchlist_service.dto.WatchlistRequest;
import com.marketquest.watchlist_service.entity.WatchlistItem;  
import com.marketquest.watchlist_service.repository.WatchlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchlistService {
    private final WatchlistRepository watchlistRepository;
    public WatchlistService(WatchlistRepository watchlistRepository) {
        this.watchlistRepository = watchlistRepository;
    }

    public List<WatchlistItem> getWatchlist(){
        return watchlistRepository.findAll();
    }

    public WatchlistItem postWatchlist(WatchlistRequest request) {
        WatchlistItem item = new WatchlistItem();
        // item.setUserId(request.getUserId());
        item.setSymbol(request.getSymbol());
        item.setCompanyName(request.getCompanyName());
        return watchlistRepository.save(item);
    }

    public WatchlistItem updateWatchlist(Long id, WatchlistRequest request) {
        WatchlistItem item = watchlistRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Stock not found"));
        item.setNote(request.getNote());
        return watchlistRepository.save(item);
    }

}